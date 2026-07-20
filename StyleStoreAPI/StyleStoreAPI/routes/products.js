// routes/products.js
const express = require('express');
const router = express.Router();
const { sql, getPool } = require('../db');
const { requireAdmin } = require('../middleware/auth');

/**
 * GET /products
 * Devuelve todos los productos activos (Activo = 1), con los MISMOS nombres
 * de columna que tiene la tabla en SQL Server (sin renombrar): el mapeo a los
 * nombres en inglés que usa la app Android se hace del lado de Android (DTO).
 *
 * Soporta un filtro opcional: /products?categoria=3
 */
router.get('/', async (req, res) => {
    try {
        const pool = await getPool();
        const request = pool.request();

        let query = `
            SELECT IdProducto, IdCategoria, Nombre, Descripcion, Precio, Stock, RutaImagen
            FROM Productos
            WHERE Activo = 1
        `;

        if (req.query.categoria) {
            request.input('idCategoria', sql.Int, parseInt(req.query.categoria, 10));
            query += ' AND IdCategoria = @idCategoria';
        }

        query += ' ORDER BY IdProducto';

        const result = await request.query(query);
        res.json(result.recordset);
    } catch (err) {
        console.error('Error en GET /products:', err.message);
        res.status(500).json({ error: 'Error al obtener los productos', detalle: err.message });
    }
});

/**
 * GET /products/:id
 * Un solo producto por su IdProducto.
 */
router.get('/:id', async (req, res) => {
    try {
        const pool = await getPool();
        const result = await pool.request()
            .input('id', sql.Int, parseInt(req.params.id, 10))
            .query(`
                SELECT IdProducto, IdCategoria, Nombre, Descripcion, Precio, Stock, RutaImagen
                FROM Productos
                WHERE IdProducto = @id AND Activo = 1
            `);

        if (result.recordset.length === 0) {
            return res.status(404).json({ error: 'Producto no encontrado' });
        }
        res.json(result.recordset[0]);
    } catch (err) {
        console.error('Error en GET /products/:id:', err.message);
        res.status(500).json({ error: 'Error al obtener el producto', detalle: err.message });
    }
});

/**
 * POST /products — SOLO Admin. Crea un producto nuevo.
 * Body: { idCategoria, nombre, descripcion, precio, stock, rutaImagen? }
 */
router.post('/', requireAdmin, async (req, res) => {
    const { idCategoria, nombre, descripcion, precio, stock, rutaImagen } = req.body;

    if (!idCategoria || !nombre || precio === undefined || stock === undefined) {
        return res.status(400).json({ error: 'Faltan campos obligatorios (idCategoria, nombre, precio, stock)' });
    }

    try {
        const pool = await getPool();
        const result = await pool.request()
            .input('idCategoria', sql.Int, idCategoria)
            .input('nombre', sql.NVarChar, nombre)
            .input('descripcion', sql.NVarChar, descripcion || null)
            .input('precio', sql.Decimal(10, 2), precio)
            .input('stock', sql.Int, stock)
            .input('rutaImagen', sql.NVarChar, rutaImagen || null)
            .query(`
                INSERT INTO Productos (IdCategoria, Nombre, Descripcion, Precio, Stock, RutaImagen)
                OUTPUT INSERTED.IdProducto, INSERTED.IdCategoria, INSERTED.Nombre, INSERTED.Descripcion,
                       INSERTED.Precio, INSERTED.Stock, INSERTED.RutaImagen
                VALUES (@idCategoria, @nombre, @descripcion, @precio, @stock, @rutaImagen)
            `);
        res.status(201).json(result.recordset[0]);
    } catch (err) {
        console.error('Error en POST /products:', err.message);
        res.status(500).json({ error: 'Error al crear el producto', detalle: err.message });
    }
});

/**
 * PUT /products/:id — SOLO Admin. Edita nombre/descripción/precio/stock/categoría.
 */
router.put('/:id', requireAdmin, async (req, res) => {
    const { idCategoria, nombre, descripcion, precio, stock, rutaImagen } = req.body;

    if (!idCategoria || !nombre || precio === undefined || stock === undefined) {
        return res.status(400).json({ error: 'Faltan campos obligatorios (idCategoria, nombre, precio, stock)' });
    }

    try {
        const pool = await getPool();
        const result = await pool.request()
            .input('id', sql.Int, parseInt(req.params.id, 10))
            .input('idCategoria', sql.Int, idCategoria)
            .input('nombre', sql.NVarChar, nombre)
            .input('descripcion', sql.NVarChar, descripcion || null)
            .input('precio', sql.Decimal(10, 2), precio)
            .input('stock', sql.Int, stock)
            .input('rutaImagen', sql.NVarChar, rutaImagen || null)
            .query(`
                UPDATE Productos
                SET IdCategoria = @idCategoria, Nombre = @nombre, Descripcion = @descripcion,
                    Precio = @precio, Stock = @stock, RutaImagen = @rutaImagen, FechaActualizacion = GETDATE()
                OUTPUT INSERTED.IdProducto, INSERTED.IdCategoria, INSERTED.Nombre, INSERTED.Descripcion,
                       INSERTED.Precio, INSERTED.Stock, INSERTED.RutaImagen
                WHERE IdProducto = @id AND Activo = 1
            `);

        if (result.recordset.length === 0) {
            return res.status(404).json({ error: 'Producto no encontrado' });
        }
        res.json(result.recordset[0]);
    } catch (err) {
        console.error('Error en PUT /products/:id:', err.message);
        res.status(500).json({ error: 'Error al actualizar el producto', detalle: err.message });
    }
});

/**
 * DELETE /products/:id — SOLO Admin. Borrado lógico (Activo = 0): nunca borra
 * la fila físicamente, porque si el producto ya está en algún
 * DetallePedido/DetalleCarrito, un DELETE real rompería esos pedidos por la
 * llave foránea.
 */
router.delete('/:id', requireAdmin, async (req, res) => {
    try {
        const pool = await getPool();
        const result = await pool.request()
            .input('id', sql.Int, parseInt(req.params.id, 10))
            .query('UPDATE Productos SET Activo = 0 WHERE IdProducto = @id');

        if (result.rowsAffected[0] === 0) {
            return res.status(404).json({ error: 'Producto no encontrado' });
        }
        res.json({ ok: true });
    } catch (err) {
        console.error('Error en DELETE /products/:id:', err.message);
        res.status(500).json({ error: 'Error al eliminar el producto', detalle: err.message });
    }
});

module.exports = router;
