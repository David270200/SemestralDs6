// scripts_hash_passwords.js
//
// Script de UNA SOLA VEZ para convertir las contraseñas en texto plano que
// dejó el script de datos de ejemplo (ej: 'admin123', '123456') en hashes
// bcrypt reales.
//
// No es obligatorio correrlo: el login (routes/auth.js) también detecta y
// "sube de nivel" automáticamente una contraseña en texto plano la primera
// vez que alguien inicia sesión con éxito. Pero si quieres dejar TODAS las
// contraseñas ya hasheadas de una vez (por ejemplo, antes de una demo),
// corre esto así:
//
//     node scripts_hash_passwords.js
//
require('dotenv').config();
const bcrypt = require('bcrypt');
const { sql, getPool } = require('./db');

// Un hash de bcrypt siempre empieza con "$2a$", "$2b$" o "$2y$".
// Si el valor guardado NO empieza así, asumimos que es texto plano.
function pareceHashBcrypt(valor) {
    return /^\$2[aby]\$/.test(valor);
}

async function main() {
    const pool = await getPool();
    const result = await pool.request().query('SELECT IdUsuario, PasswordHash FROM Usuarios');

    let actualizados = 0;
    for (const usuario of result.recordset) {
        if (!pareceHashBcrypt(usuario.PasswordHash)) {
            const nuevoHash = await bcrypt.hash(usuario.PasswordHash, 10);
            await pool.request()
                .input('id', sql.Int, usuario.IdUsuario)
                .input('hash', sql.NVarChar, nuevoHash)
                .query('UPDATE Usuarios SET PasswordHash = @hash WHERE IdUsuario = @id');
            actualizados++;
        }
    }

    console.log(`✅ Listo. ${actualizados} contraseña(s) convertida(s) a bcrypt.`);
    process.exit(0);
}

main().catch((err) => {
    console.error('❌ Error al hashear contraseñas:', err.message);
    process.exit(1);
});
