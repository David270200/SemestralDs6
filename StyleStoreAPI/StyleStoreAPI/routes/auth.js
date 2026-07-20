// routes/auth.js
const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { sql, getPool } = require('../db');

const CAMPOS_USUARIO_PUBLICOS =
    'IdUsuario, Nombre, Apellido, Correo, Telefono, Direccion, Rol, Activo, FechaRegistro';

function generarToken(usuario) {
    return jwt.sign(
        // Number(...) es importante: dependiendo del driver, IdUsuario puede
        // llegar como string o como number desde SQL Server. Si no se fuerza
        // aquí, el token puede terminar con "3" (string) en vez de 3 (number),
        // y entonces cualquier comparación estricta (===) más adelante en la
        // API (por ejemplo, "¿este carrito es tuyo?") falla aunque sea el
        // mismo usuario. Esta es la causa raíz del bug "No puedes modificar
        // el carrito de otro usuario" que ocurría con usuarios válidos.
        { IdUsuario: Number(usuario.IdUsuario), Correo: usuario.Correo, Rol: usuario.Rol },
        process.env.JWT_SECRET,
        { expiresIn: '7d' }
    );
}

// Un hash de bcrypt siempre empieza con "$2a$", "$2b$" o "$2y$".
function pareceHashBcrypt(valor) {
    return /^\$2[aby]\$/.test(valor);
}

/**
 * POST /auth/register
 * Body: { nombre, apellido, correo, password, telefono?, direccion? }
 * Crea un usuario nuevo con Rol = 'Cliente' (fijo, no lo manda el cliente).
 */
router.post('/register', async (req, res) => {
    const { nombre, apellido, correo, password, telefono, direccion } = req.body;

    if (!nombre || !apellido || !correo || !password) {
        return res.status(400).json({ error: 'Faltan campos obligatorios (nombre, apellido, correo, password)' });
    }
    if (password.length < 6) {
        return res.status(400).json({ error: 'La contraseña debe tener al menos 6 caracteres' });
    }

    try {
        const pool = await getPool();

        const existente = await pool.request()
            .input('correo', sql.NVarChar, correo)
            .query('SELECT IdUsuario FROM Usuarios WHERE Correo = @correo');

        if (existente.recordset.length > 0) {
            return res.status(409).json({ error: 'Ya existe una cuenta registrada con ese correo' });
        }

        const passwordHash = await bcrypt.hash(password, 10);

        const insertResult = await pool.request()
            .input('nombre', sql.NVarChar, nombre)
            .input('apellido', sql.NVarChar, apellido)
            .input('correo', sql.NVarChar, correo)
            .input('passwordHash', sql.NVarChar, passwordHash)
            .input('telefono', sql.NVarChar, telefono || null)
            .input('direccion', sql.NVarChar, direccion || null)
            .query(`
                INSERT INTO Usuarios (Nombre, Apellido, Correo, PasswordHash, Telefono, Direccion, Rol)
                OUTPUT INSERTED.IdUsuario
                VALUES (@nombre, @apellido, @correo, @passwordHash, @telefono, @direccion, 'Cliente')
            `);

        const nuevoId = insertResult.recordset[0].IdUsuario;

        const usuarioResult = await pool.request()
            .input('id', sql.Int, nuevoId)
            .query(`SELECT ${CAMPOS_USUARIO_PUBLICOS} FROM Usuarios WHERE IdUsuario = @id`);

        const usuario = usuarioResult.recordset[0];
        const token = generarToken(usuario);

        res.status(201).json({ token, usuario });
    } catch (err) {
        console.error('Error en POST /auth/register:', err.message);
        res.status(500).json({ error: 'Error al registrar el usuario', detalle: err.message });
    }
});

/**
 * POST /auth/login
 * Body: { correo, password }
 * Valida contra Usuarios.PasswordHash. Si el valor guardado todavía está en
 * texto plano (datos de ejemplo del script original), lo compara directo Y
 * de paso lo convierte a bcrypt para la próxima vez (auto-migración).
 */
router.post('/login', async (req, res) => {
    const { correo, password } = req.body;

    if (!correo || !password) {
        return res.status(400).json({ error: 'Faltan correo o password' });
    }

    try {
        const pool = await getPool();
        const result = await pool.request()
            .input('correo', sql.NVarChar, correo)
            .query(`SELECT IdUsuario, Nombre, Apellido, Correo, PasswordHash, Telefono, Direccion, Rol, Activo, FechaRegistro
                    FROM Usuarios WHERE Correo = @correo`);

        if (result.recordset.length === 0) {
            return res.status(401).json({ error: 'Correo o contraseña incorrectos' });
        }

        const usuario = result.recordset[0];

        if (!usuario.Activo) {
            return res.status(403).json({ error: 'Esta cuenta está inactiva' });
        }

        let passwordValida;
        if (pareceHashBcrypt(usuario.PasswordHash)) {
            passwordValida = await bcrypt.compare(password, usuario.PasswordHash);
        } else {
            // Contraseña vieja en texto plano (datos de ejemplo). Comparamos directo.
            passwordValida = password === usuario.PasswordHash;
            if (passwordValida) {
                // Aprovechamos que inició sesión bien para subirla a bcrypt ya mismo.
                const nuevoHash = await bcrypt.hash(password, 10);
                await pool.request()
                    .input('id', sql.Int, usuario.IdUsuario)
                    .input('hash', sql.NVarChar, nuevoHash)
                    .query('UPDATE Usuarios SET PasswordHash = @hash WHERE IdUsuario = @id');
            }
        }

        if (!passwordValida) {
            return res.status(401).json({ error: 'Correo o contraseña incorrectos' });
        }

        delete usuario.PasswordHash; // nunca devolver el hash al cliente
        const token = generarToken(usuario);

        res.json({ token, usuario });
    } catch (err) {
        console.error('Error en POST /auth/login:', err.message);
        res.status(500).json({ error: 'Error al iniciar sesión', detalle: err.message });
    }
});

module.exports = router;
