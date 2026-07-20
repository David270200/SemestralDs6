// routes/users.js
const express = require('express');
const router = express.Router();
const { sql, getPool } = require('../db');
const { requireAuth, requireAdmin } = require('../middleware/auth');

router.use(requireAuth);

/**
 * GET /users — SOLO Admin. Lista todos los usuarios, con la cantidad de
 * pedidos de cada uno (calculada con un COUNT, no guardada como columna).
 */
router.get('/', requireAdmin, async (req, res) => {
    try {
        const pool = await getPool();
        const result = await pool.request().query(`
            SELECT u.IdUsuario, u.Nombre, u.Apellido, u.Correo, u.Telefono, u.Direccion,
                   u.Rol, u.Activo, u.FechaRegistro,
                   (SELECT COUNT(*) FROM Pedidos p WHERE p.IdUsuario = u.IdUsuario) AS CantidadPedidos
            FROM Usuarios u
            ORDER BY u.IdUsuario
        `);
        res.json(result.recordset);
    } catch (err) {
        console.error('Error en GET /users:', err.message);
        res.status(500).json({ error: 'Error al obtener los usuarios', detalle: err.message });
    }
});

/** PUT /users/:id/estado — SOLO Admin. Activa/Inactiva una cuenta. Body: { activo: true|false } */
router.put('/:id/estado', requireAdmin, async (req, res) => {
    const { activo } = req.body;
    if (typeof activo !== 'boolean') {
        return res.status(400).json({ error: 'Falta "activo" (true/false) en el body' });
    }

    try {
        const pool = await getPool();
        const result = await pool.request()
            .input('id', sql.Int, parseInt(req.params.id, 10))
            .input('activo', sql.Bit, activo)
            .query('UPDATE Usuarios SET Activo = @activo WHERE IdUsuario = @id');

        if (result.rowsAffected[0] === 0) {
            return res.status(404).json({ error: 'Usuario no encontrado' });
        }
        res.json({ ok: true });
    } catch (err) {
        console.error('Error en PUT /users/:id/estado:', err.message);
        res.status(500).json({ error: 'Error al actualizar el usuario', detalle: err.message });
    }
});

/**
 * PUT /users/me — Cualquier usuario logueado edita SU PROPIO perfil.
 * Body: { nombre, apellido, telefono?, direccion? } (el correo y el rol NO se editan aquí).
 */
router.put('/me', async (req, res) => {
    const { nombre, apellido, telefono, direccion } = req.body;
    if (!nombre || !apellido) {
        return res.status(400).json({ error: 'Faltan nombre y/o apellido' });
    }

    try {
        const pool = await getPool();
        const result = await pool.request()
            .input('id', sql.Int, Number(req.usuario.IdUsuario))
            .input('nombre', sql.NVarChar, nombre)
            .input('apellido', sql.NVarChar, apellido)
            .input('telefono', sql.NVarChar, telefono || null)
            .input('direccion', sql.NVarChar, direccion || null)
            .query(`
                UPDATE Usuarios
                SET Nombre = @nombre, Apellido = @apellido, Telefono = @telefono, Direccion = @direccion
                OUTPUT INSERTED.IdUsuario, INSERTED.Nombre, INSERTED.Apellido, INSERTED.Correo,
                       INSERTED.Telefono, INSERTED.Direccion, INSERTED.Rol, INSERTED.Activo, INSERTED.FechaRegistro
                WHERE IdUsuario = @id
            `);

        res.json(result.recordset[0]);
    } catch (err) {
        console.error('Error en PUT /users/me:', err.message);
        res.status(500).json({ error: 'Error al actualizar tu perfil', detalle: err.message });
    }
});

module.exports = router;
