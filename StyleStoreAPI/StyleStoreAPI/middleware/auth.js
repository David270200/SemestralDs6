// middleware/auth.js
//
// Protege rutas que requieren estar logueado. Lee el token JWT del header
// "Authorization: Bearer <token>", lo valida, y si es correcto agrega
// req.usuario = { IdUsuario, Correo, Rol } para que la ruta lo use.
const jwt = require('jsonwebtoken');

function requireAuth(req, res, next) {
    const header = req.headers['authorization'];
    if (!header || !header.startsWith('Bearer ')) {
        return res.status(401).json({ error: 'No se envió un token de autenticación (Authorization: Bearer <token>)' });
    }

    const token = header.substring('Bearer '.length);

    try {
        const payload = jwt.verify(token, process.env.JWT_SECRET);
        req.usuario = payload; // { IdUsuario, Correo, Rol }
        next();
    } catch (err) {
        return res.status(401).json({ error: 'Token inválido o expirado, inicia sesión de nuevo' });
    }
}

/** Además de estar logueado, exige que el Rol sea 'Administrador'. */
function requireAdmin(req, res, next) {
    requireAuth(req, res, () => {
        if (req.usuario.Rol !== 'Administrador') {
            return res.status(403).json({ error: 'Esta acción requiere permisos de administrador' });
        }
        next();
    });
}

module.exports = { requireAuth, requireAdmin };
