// db.js
// Maneja la conexión a SQL Server (LocalDB) usando Windows Authentication real
// a través del driver ODBC (paquete "msnodesqlv8"). Por eso NO hay usuario/contraseña
// en la configuración: se usa la sesión de Windows con la que corre "node index.js".

require('dotenv').config();
const sql = require('mssql/msnodesqlv8');

const driver = process.env.DB_DRIVER || 'ODBC Driver 17 for SQL Server';
const server = process.env.DB_SERVER || '(localdb)\\MSSQLLocalDB';
const database = process.env.DB_DATABASE || 'StyleStoreDB';

// Cadena de conexión ODBC. "Trusted_Connection=Yes" es lo que le dice a SQL Server
// "usa la sesión de Windows actual", en vez de pedir usuario/contraseña.
const connectionString =
    `Driver={${driver}};Server=${server};Database=${database};Trusted_Connection=Yes;`;

const config = {
    connectionString
};

let pool = null;

/**
 * Devuelve un pool de conexiones ya conectado (lo reutiliza si ya existe).
 * Se usa desde cada ruta en vez de abrir una conexión nueva por cada request,
 * que sería muy lento.
 */
async function getPool() {
    if (pool) return pool;

    try {
        pool = await sql.connect(config);
        console.log('✅ Conectado a SQL Server:', server, '/', database);
        return pool;
    } catch (err) {
        pool = null;
        console.error('❌ No se pudo conectar a SQL Server.');
        console.error('   Server:', server, '| Database:', database, '| Driver:', driver);
        console.error('   Motivo:', err.message);
        throw err;
    }
}

module.exports = { sql, getPool };
