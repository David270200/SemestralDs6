// routes/cart.js
const express = require('express');
const router = express.Router();
const { sql, getPool } = require('../db');
const { requireAuth } = require('../middleware/auth');

const UMBRAL_ENVIO_GRATIS = 50.0; // Regla de negocio: bajo $50 solo "Retiro en tienda"

// Todas las rutas de carrito requieren estar logueado.
router.use(requireAuth);

/** Solo el propio usuario (o un Administrador) puede tocar su carrito. */
function verificarPropietario(req, res) {
    const idUsuarioParam = parseInt(req.params.idUsuario, 10);
    // Number(...) en ambos lados: el IdUsuario del token o de una consulta SQL
    // puede llegar como string o como number según el driver: comparar con
    // !== sin normalizar el tipo primero puede dar un falso "no coincide"
    // aunque sea el mismo usuario. Ver nota en routes/auth.js (generarToken).
    if (Number(req.usuario.IdUsuario) !== Number(idUsuarioParam) && req.usuario.Rol !== 'Administrador') {
        res.status(403).json({ error: 'No puedes modificar el carrito de otro usuario' });
        return false;
    }
    return true;
}

/** Devuelve el IdCarrito del usuario, creándolo si todavía no existe. */
async function obtenerOCrearCarrito(pool, idUsuario) {
    const existente = await pool.request()
        .input('idUsuario', sql.Int, idUsuario)
        .query('SELECT IdCarrito FROM Carrito WHERE IdUsuario = @idUsuario');

    if (existente.recordset.length > 0) {
        return existente.recordset[0].IdCarrito;
    }

    const creado = await pool.request()
        .input('idUsuario', sql.Int, idUsuario)
        .query('INSERT INTO Carrito (IdUsuario) OUTPUT INSERTED.IdCarrito VALUES (@idUsuario)');

    return creado.recordset[0].IdCarrito;
}

/** Arma la respuesta { items, subtotal, homeDeliveryAvailable } para un carrito. */
async function armarRespuestaCarrito(pool, idCarrito) {
    const detalle = await pool.request()
        .input('idCarrito', sql.Int, idCarrito)
        .query(`
            SELECT p.IdProducto, p.Nombre, p.Precio, p.Stock, p.RutaImagen, dc.Cantidad
            FROM DetalleCarrito dc
            INNER JOIN Productos p ON p.IdProducto = dc.IdProducto
            WHERE dc.IdCarrito = @idCarrito
            ORDER BY dc.IdDetalleCarrito
        `);

    const items = detalle.recordset;
    const subtotal = items.reduce((acc, item) => acc + (item.Precio * item.Cantidad), 0);

    return {
        items,
        subtotal: Math.round(subtotal * 100) / 100,
        homeDeliveryAvailable: subtotal >= UMBRAL_ENVIO_GRATIS
    };
}

/** GET /cart/:idUsuario — trae el carrito completo. */
router.get('/:idUsuario', async (req, res) => {
    if (!verificarPropietario(req, res)) return;

    try {
        const pool = await getPool();
        const idCarrito = await obtenerOCrearCarrito(pool, parseInt(req.params.idUsuario, 10));
        res.json(await armarRespuestaCarrito(pool, idCarrito));
    } catch (err) {
        console.error('Error en GET /cart/:idUsuario:', err.message);
        res.status(500).json({ error: 'Error al obtener el carrito', detalle: err.message });
    }
});

/** POST /cart/:idUsuario/items — Body: { idProducto, cantidad }. Agrega o suma cantidad. */
router.post('/:idUsuario/items', async (req, res) => {
    if (!verificarPropietario(req, res)) return;

    const { idProducto, cantidad } = req.body;
    if (!idProducto || !cantidad || cantidad <= 0) {
        return res.status(400).json({ error: 'Se necesita idProducto y cantidad (mayor a 0)' });
    }

    try {
        const pool = await getPool();
        const idCarrito = await obtenerOCrearCarrito(pool, parseInt(req.params.idUsuario, 10));

        const existente = await pool.request()
            .input('idCarrito', sql.Int, idCarrito)
            .input('idProducto', sql.Int, idProducto)
            .query('SELECT Cantidad FROM DetalleCarrito WHERE IdCarrito = @idCarrito AND IdProducto = @idProducto');

        if (existente.recordset.length > 0) {
            const nuevaCantidad = existente.recordset[0].Cantidad + cantidad;
            await pool.request()
                .input('idCarrito', sql.Int, idCarrito)
                .input('idProducto', sql.Int, idProducto)
                .input('cantidad', sql.Int, nuevaCantidad)
                .query('UPDATE DetalleCarrito SET Cantidad = @cantidad WHERE IdCarrito = @idCarrito AND IdProducto = @idProducto');
        } else {
            await pool.request()
                .input('idCarrito', sql.Int, idCarrito)
                .input('idProducto', sql.Int, idProducto)
                .input('cantidad', sql.Int, cantidad)
                .query('INSERT INTO DetalleCarrito (IdCarrito, IdProducto, Cantidad) VALUES (@idCarrito, @idProducto, @cantidad)');
        }

        res.json(await armarRespuestaCarrito(pool, idCarrito));
    } catch (err) {
        console.error('Error en POST /cart/:idUsuario/items:', err.message);
        res.status(500).json({ error: 'Error al agregar al carrito', detalle: err.message });
    }
});

/** PUT /cart/:idUsuario/items/:idProducto — Body: { cantidad }. cantidad <= 0 elimina el item. */
router.put('/:idUsuario/items/:idProducto', async (req, res) => {
    if (!verificarPropietario(req, res)) return;

    const { cantidad } = req.body;
    if (cantidad === undefined) {
        return res.status(400).json({ error: 'Se necesita "cantidad" en el body' });
    }

    try {
        const pool = await getPool();
        const idCarrito = await obtenerOCrearCarrito(pool, parseInt(req.params.idUsuario, 10));
        const idProducto = parseInt(req.params.idProducto, 10);

        if (cantidad <= 0) {
            await pool.request()
                .input('idCarrito', sql.Int, idCarrito)
                .input('idProducto', sql.Int, idProducto)
                .query('DELETE FROM DetalleCarrito WHERE IdCarrito = @idCarrito AND IdProducto = @idProducto');
        } else {
            await pool.request()
                .input('idCarrito', sql.Int, idCarrito)
                .input('idProducto', sql.Int, idProducto)
                .input('cantidad', sql.Int, cantidad)
                .query('UPDATE DetalleCarrito SET Cantidad = @cantidad WHERE IdCarrito = @idCarrito AND IdProducto = @idProducto');
        }

        res.json(await armarRespuestaCarrito(pool, idCarrito));
    } catch (err) {
        console.error('Error en PUT /cart/:idUsuario/items/:idProducto:', err.message);
        res.status(500).json({ error: 'Error al actualizar el carrito', detalle: err.message });
    }
});

/** DELETE /cart/:idUsuario/items/:idProducto — quita un producto del carrito. */
router.delete('/:idUsuario/items/:idProducto', async (req, res) => {
    if (!verificarPropietario(req, res)) return;

    try {
        const pool = await getPool();
        const idCarrito = await obtenerOCrearCarrito(pool, parseInt(req.params.idUsuario, 10));

        await pool.request()
            .input('idCarrito', sql.Int, idCarrito)
            .input('idProducto', sql.Int, parseInt(req.params.idProducto, 10))
            .query('DELETE FROM DetalleCarrito WHERE IdCarrito = @idCarrito AND IdProducto = @idProducto');

        res.json(await armarRespuestaCarrito(pool, idCarrito));
    } catch (err) {
        console.error('Error en DELETE /cart/:idUsuario/items/:idProducto:', err.message);
        res.status(500).json({ error: 'Error al eliminar del carrito', detalle: err.message });
    }
});

/** DELETE /cart/:idUsuario — vacía el carrito completo (se usa después del checkout). */
router.delete('/:idUsuario', async (req, res) => {
    if (!verificarPropietario(req, res)) return;

    try {
        const pool = await getPool();
        const idCarrito = await obtenerOCrearCarrito(pool, parseInt(req.params.idUsuario, 10));

        await pool.request()
            .input('idCarrito', sql.Int, idCarrito)
            .query('DELETE FROM DetalleCarrito WHERE IdCarrito = @idCarrito');

        res.json(await armarRespuestaCarrito(pool, idCarrito));
    } catch (err) {
        console.error('Error en DELETE /cart/:idUsuario:', err.message);
        res.status(500).json({ error: 'Error al vaciar el carrito', detalle: err.message });
    }
});

module.exports = router;
