// routes/orders.js
const express = require('express');
const router = express.Router();
const { sql, getPool } = require('../db');
const { requireAuth, requireAdmin } = require('../middleware/auth');

const UMBRAL_ENVIO_GRATIS = 50.0;
const COSTO_ENVIO = 5.0;
const ESTADOS_VALIDOS = ['Pendiente', 'Preparando', 'Enviado', 'Entregado', 'Cancelado'];

router.use(requireAuth); // todas las rutas de pedidos requieren sesión

/**
 * POST /orders — Crea el pedido a partir del CARRITO ACTUAL del usuario logueado.
 * Body: { metodoEntrega: "Retiro en tienda" | "Envío a domicilio" }
 *
 * Hace, en UNA sola transacción (todo o nada):
 *  1. Valida la regla de negocio de los $50 del lado del servidor (no solo Android).
 *  2. Valida que haya stock suficiente de cada producto.
 *  3. Crea el Pedido.
 *  4. Crea un DetallePedido por cada producto del carrito.
 *  5. Descuenta el Stock de cada producto.
 *  6. Vacía el carrito (DetalleCarrito) del usuario.
 * Si cualquier paso falla, se revierte TODO (rollback) — nunca queda un pedido
 * a medias ni stock descontado sin su pedido correspondiente.
 */
router.post('/', async (req, res) => {
    const { metodoEntrega } = req.body;
    const idUsuario = Number(req.usuario.IdUsuario);

    if (metodoEntrega !== 'Retiro en tienda' && metodoEntrega !== 'Envío a domicilio') {
        return res.status(400).json({ error: 'metodoEntrega debe ser "Retiro en tienda" o "Envío a domicilio"' });
    }

    const pool = await getPool();

    // Traemos el carrito FUERA de la transacción (solo lectura), para saber qué validar.
    const carritoResult = await pool.request()
        .input('idUsuario', sql.Int, idUsuario)
        .query('SELECT IdCarrito FROM Carrito WHERE IdUsuario = @idUsuario');

    if (carritoResult.recordset.length === 0) {
        return res.status(400).json({ error: 'Tu carrito está vacío' });
    }
    const idCarrito = carritoResult.recordset[0].IdCarrito;

    const itemsResult = await pool.request()
        .input('idCarrito', sql.Int, idCarrito)
        .query(`
            SELECT p.IdProducto, p.Nombre, p.Precio, p.Stock, dc.Cantidad
            FROM DetalleCarrito dc
            INNER JOIN Productos p ON p.IdProducto = dc.IdProducto
            WHERE dc.IdCarrito = @idCarrito
        `);
    const items = itemsResult.recordset;

    if (items.length === 0) {
        return res.status(400).json({ error: 'Tu carrito está vacío' });
    }

    const subtotal = items.reduce((acc, item) => acc + (item.Precio * item.Cantidad), 0);

    // Regla de negocio validada del lado del servidor, igual que en Android.
    if (metodoEntrega === 'Envío a domicilio' && subtotal < UMBRAL_ENVIO_GRATIS) {
        return res.status(400).json({ error: `El envío a domicilio requiere un subtotal mínimo de $${UMBRAL_ENVIO_GRATIS.toFixed(2)}` });
    }

    // Validamos stock ANTES de abrir la transacción, para devolver un mensaje claro.
    const sinStock = items.find((item) => item.Stock < item.Cantidad);
    if (sinStock) {
        return res.status(409).json({ error: `No hay stock suficiente de "${sinStock.Nombre}" (disponible: ${sinStock.Stock}, pedido: ${sinStock.Cantidad})` });
    }

    const deliveryFee = metodoEntrega === 'Envío a domicilio' ? COSTO_ENVIO : 0.0;
    const total = subtotal + deliveryFee;

    const transaction = new sql.Transaction(pool);

    try {
        await transaction.begin();

        // 1. Crear el Pedido (con un NumeroPedido temporal, lo actualizamos abajo).
        const pedidoInsert = await new sql.Request(transaction)
            .input('idUsuario', sql.Int, idUsuario)
            .input('metodoEntrega', sql.NVarChar, metodoEntrega)
            .input('total', sql.Decimal(10, 2), total)
            .query(`
                INSERT INTO Pedidos (NumeroPedido, IdUsuario, MetodoEntrega, Estado, Total)
                OUTPUT INSERTED.IdPedido
                VALUES ('TEMP', @idUsuario, @metodoEntrega, 'Pendiente', @total)
            `);
        const idPedido = pedidoInsert.recordset[0].IdPedido;

        // 2. Generar el número real de pedido, mismo formato que tus datos de ejemplo (ST-20260001).
        const numeroPedido = `ST-${new Date().getFullYear()}${String(idPedido).padStart(4, '0')}`;
        await new sql.Request(transaction)
            .input('idPedido', sql.Int, idPedido)
            .input('numeroPedido', sql.NVarChar, numeroPedido)
            .query('UPDATE Pedidos SET NumeroPedido = @numeroPedido WHERE IdPedido = @idPedido');

        // 3. Un DetallePedido por cada producto, y 4. descontar Stock (con chequeo atómico).
        for (const item of items) {
            const subtotalItem = item.Precio * item.Cantidad;

            await new sql.Request(transaction)
                .input('idPedido', sql.Int, idPedido)
                .input('idProducto', sql.Int, item.IdProducto)
                .input('cantidad', sql.Int, item.Cantidad)
                .input('precioUnitario', sql.Decimal(10, 2), item.Precio)
                .input('subtotal', sql.Decimal(10, 2), subtotalItem)
                .query(`
                    INSERT INTO DetallePedidos (IdPedido, IdProducto, Cantidad, PrecioUnitario, Subtotal)
                    VALUES (@idPedido, @idProducto, @cantidad, @precioUnitario, @subtotal)
                `);

            const stockUpdate = await new sql.Request(transaction)
                .input('idProducto', sql.Int, item.IdProducto)
                .input('cantidad', sql.Int, item.Cantidad)
                .query(`
                    UPDATE Productos SET Stock = Stock - @cantidad
                    WHERE IdProducto = @idProducto AND Stock >= @cantidad
                `);

            // Si otra venta bajó el stock justo entre la validación de arriba y este UPDATE,
            // rowsAffected da 0 -> abortamos TODO el pedido (nadie se queda con stock negativo).
            if (stockUpdate.rowsAffected[0] === 0) {
                throw new Error(`Sin stock suficiente de "${item.Nombre}" al momento de confirmar`);
            }
        }

        // 5. Vaciar el carrito.
        await new sql.Request(transaction)
            .input('idCarrito', sql.Int, idCarrito)
            .query('DELETE FROM DetalleCarrito WHERE IdCarrito = @idCarrito');

        await transaction.commit();

        res.status(201).json(await obtenerPedidoCompleto(pool, idPedido));
    } catch (err) {
        await transaction.rollback();
        console.error('Error en POST /orders (rollback aplicado):', err.message);
        res.status(500).json({ error: 'No se pudo confirmar el pedido', detalle: err.message });
    }
});

/** Arma un pedido completo (cabecera + items) para la Factura / detalle. */
async function obtenerPedidoCompleto(pool, idPedido) {
    const cabeceraResult = await pool.request()
        .input('idPedido', sql.Int, idPedido)
        .query(`
            SELECT ped.IdPedido, ped.NumeroPedido, ped.IdUsuario, ped.FechaPedido, ped.FechaEntrega,
                   ped.MetodoEntrega, ped.Estado, ped.Total,
                   u.Nombre AS ClienteNombre, u.Apellido AS ClienteApellido, u.Correo AS ClienteCorreo
            FROM Pedidos ped
            INNER JOIN Usuarios u ON u.IdUsuario = ped.IdUsuario
            WHERE ped.IdPedido = @idPedido
        `);

    if (cabeceraResult.recordset.length === 0) return null;
    const pedido = cabeceraResult.recordset[0];

    const itemsResult = await pool.request()
        .input('idPedido', sql.Int, idPedido)
        .query(`
            SELECT dp.IdProducto, p.Nombre, dp.Cantidad, dp.PrecioUnitario, dp.Subtotal
            FROM DetallePedidos dp
            INNER JOIN Productos p ON p.IdProducto = dp.IdProducto
            WHERE dp.IdPedido = @idPedido
        `);

    pedido.items = itemsResult.recordset;
    return pedido;
}

/**
 * GET /orders — Clientes ven SOLO sus pedidos. Administradores ven TODOS.
 */
router.get('/', async (req, res) => {
    try {
        const pool = await getPool();
        const esAdmin = req.usuario.Rol === 'Administrador';

        const request = pool.request();
        let query = `
            SELECT ped.IdPedido, ped.NumeroPedido, ped.IdUsuario, ped.FechaPedido, ped.FechaEntrega,
                   ped.MetodoEntrega, ped.Estado, ped.Total,
                   u.Nombre AS ClienteNombre, u.Apellido AS ClienteApellido, u.Correo AS ClienteCorreo
            FROM Pedidos ped
            INNER JOIN Usuarios u ON u.IdUsuario = ped.IdUsuario
        `;

        if (!esAdmin) {
            request.input('idUsuario', sql.Int, Number(req.usuario.IdUsuario));
            query += ' WHERE ped.IdUsuario = @idUsuario';
        }

        query += ' ORDER BY ped.IdPedido DESC';

        const result = await request.query(query);
        res.json(result.recordset);
    } catch (err) {
        console.error('Error en GET /orders:', err.message);
        res.status(500).json({ error: 'Error al obtener los pedidos', detalle: err.message });
    }
});

/**
 * GET /orders/:id — Detalle completo (para la Factura). Solo el dueño del
 * pedido o un Administrador pueden verlo.
 */
router.get('/:id', async (req, res) => {
    try {
        const pool = await getPool();
        const idPedido = parseInt(req.params.id, 10);
        const pedido = await obtenerPedidoCompleto(pool, idPedido);

        if (!pedido) {
            return res.status(404).json({ error: 'Pedido no encontrado' });
        }
        if (Number(pedido.IdUsuario) !== Number(req.usuario.IdUsuario) && req.usuario.Rol !== 'Administrador') {
            return res.status(403).json({ error: 'No puedes ver un pedido de otro usuario' });
        }

        res.json(pedido);
    } catch (err) {
        console.error('Error en GET /orders/:id:', err.message);
        res.status(500).json({ error: 'Error al obtener el pedido', detalle: err.message });
    }
});

/**
 * PATCH /orders/:id/status — SOLO Admin. Body: { estado }
 * estado debe ser uno de: Pendiente, Preparando, Enviado, Entregado, Cancelado.
 */
router.patch('/:id/status', requireAdmin, async (req, res) => {
    const { estado } = req.body;
    if (!ESTADOS_VALIDOS.includes(estado)) {
        return res.status(400).json({ error: `estado debe ser uno de: ${ESTADOS_VALIDOS.join(', ')}` });
    }

    try {
        const pool = await getPool();
        const idPedido = parseInt(req.params.id, 10);

        // Si se cancela un pedido, devolvemos el stock que se había descontado.
        if (estado === 'Cancelado') {
            const yaResult = await pool.request()
                .input('idPedido', sql.Int, idPedido)
                .query('SELECT Estado FROM Pedidos WHERE IdPedido = @idPedido');

            if (yaResult.recordset.length === 0) {
                return res.status(404).json({ error: 'Pedido no encontrado' });
            }

            if (yaResult.recordset[0].Estado !== 'Cancelado') {
                const detalle = await pool.request()
                    .input('idPedido', sql.Int, idPedido)
                    .query('SELECT IdProducto, Cantidad FROM DetallePedidos WHERE IdPedido = @idPedido');

                for (const item of detalle.recordset) {
                    await pool.request()
                        .input('idProducto', sql.Int, item.IdProducto)
                        .input('cantidad', sql.Int, item.Cantidad)
                        .query('UPDATE Productos SET Stock = Stock + @cantidad WHERE IdProducto = @idProducto');
                }
            }
        }

        const result = await pool.request()
            .input('idPedido', sql.Int, idPedido)
            .input('estado', sql.NVarChar, estado)
            .input('fechaEntrega', sql.DateTime, estado === 'Entregado' ? new Date() : null)
            .query(`
                UPDATE Pedidos
                SET Estado = @estado, FechaEntrega = COALESCE(@fechaEntrega, FechaEntrega)
                WHERE IdPedido = @idPedido
            `);

        if (result.rowsAffected[0] === 0) {
            return res.status(404).json({ error: 'Pedido no encontrado' });
        }

        res.json(await obtenerPedidoCompleto(pool, idPedido));
    } catch (err) {
        console.error('Error en PATCH /orders/:id/status:', err.message);
        res.status(500).json({ error: 'Error al actualizar el estado', detalle: err.message });
    }
});

module.exports = router;
