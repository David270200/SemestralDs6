// index.js
// Punto de entrada de la API de StyleStore.

require('dotenv').config();
const path = require('path');
const express = require('express');
const cors = require('cors');
const { getPool } = require('./db');

const productsRouter = require('./routes/products');
const categoriesRouter = require('./routes/categories');
const authRouter = require('./routes/auth');
const cartRouter = require('./routes/cart');
const ordersRouter = require('./routes/orders');
const usersRouter = require('./routes/users');

const app = express();
const PORT = process.env.PORT || 3000;

// cors() sin opciones permite llamadas desde cualquier origen. Como esta API
// solo corre en tu red local durante el desarrollo, no representa un riesgo real;
// si algún día la publicas en internet, aquí se restringiría a orígenes específicos.
app.use(cors());
app.use(express.json());

// Sirve las imágenes de productos como archivos estáticos. Con esto, el valor
// que ya tiene guardado Productos.RutaImagen (ej: "images/products/camisa1.jpg")
// se convierte directamente en una URL real: http://<tu-ip>:3000/images/products/camisa1.jpg
// No hace falta tocar ninguna fila de la base de datos.
app.use('/images', express.static(path.join(__dirname, 'public', 'images')));

// Ruta simple para verificar rápido, desde el navegador o Postman, que el
// servidor está corriendo y si logró conectarse a SQL Server.
app.get('/health', async (req, res) => {
    try {
        await getPool();
        res.json({ status: 'ok', database: 'conectado' });
    } catch (err) {
        res.status(500).json({ status: 'error', database: 'desconectado', detalle: err.message });
    }
});

app.use('/products', productsRouter);
app.use('/categories', categoriesRouter);
app.use('/auth', authRouter);
app.use('/cart', cartRouter);
app.use('/orders', ordersRouter);
app.use('/users', usersRouter);

// Manejador de rutas no encontradas
app.use((req, res) => {
    res.status(404).json({ error: `Ruta no encontrada: ${req.method} ${req.originalUrl}` });
});

async function start() {
    try {
        // Probamos la conexión a SQL Server ANTES de empezar a aceptar pedidos,
        // así el error aparece claro en la consola apenas arrancas el servidor,
        // en vez de fallar silenciosamente en el primer request de la app.
        await getPool();
    } catch (err) {
        console.error('⚠️  El servidor va a arrancar igual, pero las rutas que usan la base de datos van a fallar hasta que se resuelva la conexión.');
    }

    app.listen(PORT, '0.0.0.0', () => {
        console.log(`🚀 StyleStore API corriendo en http://localhost:${PORT}`);
        console.log(`   Prueba de salud: http://localhost:${PORT}/health`);
        console.log(`   Desde el emulador Android: http://10.0.2.2:${PORT}/`);
    });
}

start();
