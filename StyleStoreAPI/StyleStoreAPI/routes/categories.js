// routes/categories.js
const express = require('express');
const router = express.Router();
const { sql, getPool } = require('../db');
const { requireAdmin } = require('../middleware/auth');

/** GET /categories — todas las categorías activas. */
router.get('/', async (req, res) => {
    try {
        const pool = await getPool();
        const result = await pool.request().query(`
            SELECT IdCategoria, Nombre, Descripcion
            FROM Categorias
            WHERE Activo = 1
            ORDER BY IdCategoria
        `);
        res.json(result.recordset);
    } catch (err) {
        console.error('Error en GET /categories:', err.message);
        res.status(500).json({ error: 'Error al obtener las categorías', detalle: err.message });
    }
});

/** POST /categories — SOLO Admin. Body: { nombre, descripcion? } */
router.post('/', requireAdmin, async (req, res) => {
    const { nombre, descripcion } = req.body;
    if (!nombre) {
        return res.status(400).json({ error: 'Falta el nombre de la categoría' });
    }

    try {
        const pool = await getPool();
        const result = await pool.request()
            .input('nombre', sql.NVarChar, nombre)
            .input('descripcion', sql.NVarChar, descripcion || null)
            .query(`
                INSERT INTO Categorias (Nombre, Descripcion)
                OUTPUT INSERTED.IdCategoria, INSERTED.Nombre, INSERTED.Descripcion
                VALUES (@nombre, @descripcion)
            `);
        res.status(201).json(result.recordset[0]);
    } catch (err) {
        console.error('Error en POST /categories:', err.message);
        res.status(500).json({ error: 'Error al crear la categoría', detalle: err.message });
    }
});

/** PUT /categories/:id — SOLO Admin. */
router.put('/:id', requireAdmin, async (req, res) => {
    const { nombre, descripcion } = req.body;
    if (!nombre) {
        return res.status(400).json({ error: 'Falta el nombre de la categoría' });
    }

    try {
        const pool = await getPool();
        const result = await pool.request()
            .input('id', sql.Int, parseInt(req.params.id, 10))
            .input('nombre', sql.NVarChar, nombre)
            .input('descripcion', sql.NVarChar, descripcion || null)
            .query(`
                UPDATE Categorias
                SET Nombre = @nombre, Descripcion = @descripcion
                OUTPUT INSERTED.IdCategoria, INSERTED.Nombre, INSERTED.Descripcion
                WHERE IdCategoria = @id AND Activo = 1
            `);

        if (result.recordset.length === 0) {
            return res.status(404).json({ error: 'Categoría no encontrada' });
        }
        res.json(result.recordset[0]);
    } catch (err) {
        console.error('Error en PUT /categories/:id:', err.message);
        res.status(500).json({ error: 'Error al actualizar la categoría', detalle: err.message });
    }
});

/** DELETE /categories/:id — SOLO Admin. Borrado lógico (Activo = 0), misma razón que Productos. */
router.delete('/:id', requireAdmin, async (req, res) => {
    try {
        const pool = await getPool();
        const result = await pool.request()
            .input('id', sql.Int, parseInt(req.params.id, 10))
            .query('UPDATE Categorias SET Activo = 0 WHERE IdCategoria = @id');

        if (result.rowsAffected[0] === 0) {
            return res.status(404).json({ error: 'Categoría no encontrada' });
        }
        res.json({ ok: true });
    } catch (err) {
        console.error('Error en DELETE /categories/:id:', err.message);
        res.status(500).json({ error: 'Error al eliminar la categoría', detalle: err.message });
    }
});

module.exports = router;
