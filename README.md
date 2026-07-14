# Asistente Planificador (Tienda de Ropa)

Aplicación móvil para una tienda de ropa que incluye catálogo de productos, carrito de compras y gestión de pedidos con opciones flexibles de entrega. Diseñada para evitar sobreventas de inventario y manejar claramente el estado logístico de las compras.

## Tecnologías Utilizadas

- **Frontend:** [React Native](https://reactnative.dev/) con [Expo](https://expo.dev/)
- **Base de Datos & Autenticación:** [Supabase](https://supabase.com/) (PostgreSQL)
- **ORM:** [Prisma](https://www.prisma.io/)

## Estructura del Proyecto

El proyecto está organizado de la siguiente manera:

```text
/
├── docs/                 # Documentación del proyecto (PRD, Base de datos, Historias de Usuario, etc.)
│   ├── DATABASE.md       # Esquema y diseño de la base de datos
│   └── PRD/
│       └── PRD.md        # Documento de Requisitos del Producto
├── prisma/               # Configuración de Prisma y esquema de la base de datos
│   └── schema.prisma     # Definición de tablas y relaciones
├── src/                  # Código fuente principal de la aplicación móvil Expo
└── CONTEXT.md            # Glosario de dominio del negocio (Términos clave)
```

## Documentación

Para entender en detalle las reglas del negocio, flujos y arquitectura, por favor revisa:
1. [CONTEXT.md](./CONTEXT.md) - Define el vocabulario canónico del proyecto (Variante, Pedido, Carrito, etc.).
2. [PRD (Documento de Requisitos)](./docs/PRD/PRD.md) - Contiene las historias de usuario y el alcance del sistema.
3. [Base de Datos](./docs/DATABASE.md) - Explica el modelo relacional y la estrategia de control de stock.

## Configuración y Arranque (Desarrollo)

### 1. Instalación de Dependencias
```bash
npm install
```

### 2. Configurar Variables de Entorno
Crea o edita el archivo `.env` en la raíz del proyecto y agrega la conexión directa a tu base de datos de Supabase (puerto 5432):
```env
DATABASE_URL="postgresql://postgres.[PROYECTO]:[PASSWORD]@[HOST]:5432/postgres"
```

### 3. Sincronizar Prisma
Asegúrate de que tu base de datos local tenga las últimas tablas y genera el cliente de Prisma:
```bash
npx prisma db push
npx prisma generate
```

### 4. Ejecutar la Aplicación Móvil
Para iniciar el entorno de desarrollo con Expo:
```bash
npm run start
```
Luego podrás escanear el código QR con la app **Expo Go** en tu dispositivo, o presionar `a` para abrir el emulador de Android.