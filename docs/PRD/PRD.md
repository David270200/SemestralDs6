## Problem Statement

El usuario final necesita una aplicación móvil conveniente para explorar el catálogo de una tienda de ropa, gestionar un carrito de compras y realizar pedidos con opciones flexibles de entrega (retiro en tienda o envío a domicilio condicionado). La tienda necesita evitar sobreventas de inventario y manejar claramente el estado logístico de las entregas.

## Solution

Una aplicación móvil interactiva que permite a los usuarios buscar ropa, ver detalles y variantes (tallas y colores) y agregar productos a un Carrito. El sistema valida el inventario en el momento de realizar el pedido para asegurar que la Variante existe. Si el subtotal de la compra supera los $50, el usuario podrá elegir envío a domicilio (ingresando su Dirección de Envío para ese pedido específico). Se implementa autenticación mediante terceros (Google/Firebase) y no se permiten cancelaciones desde la app para mantener el sistema simple.

## User Stories

1. As a usuario, I want a pantalla de inicio de sesión con proveedores de terceros, so that puedo acceder a mi historial sin tener que crear otra contraseña.
2. As a usuario, I want a ver un catálogo de productos, so that puedo buscar por nombre o filtrar por categorías.
3. As a usuario, I want a ver el detalle de un Producto, so that puedo consultar sus tallas, colores disponibles y precio.
4. As a usuario, I want a poder seleccionar una Variante específica y agregarla a mi Carrito, so that puedo recolectar varios artículos antes de pagar.
5. As a usuario, I want a modificar la cantidad de una Variante en el Carrito o eliminarla, so that puedo corregir mi pedido antes de confirmar.
6. As a usuario, I want a ver el Subtotal de mi compra, so that sé cuánto dinero voy a gastar.
7. As a usuario, I want a seleccionar "Retiro en tienda" sin importar el monto, so that puedo ir a buscar mi ropa físicamente.
8. As a usuario con un carrito de $50 o más, I want a seleccionar "Envío a domicilio", so that me envíen la ropa a mi casa gratis.
9. As a usuario eligiendo "Envío a domicilio", I want a ingresar una Dirección de Envío específica para ese pedido, so that puedo enviarlo a donde me convenga en ese momento.
10. As a usuario, I want a confirmar mi pedido con un solo botón, so that la app verifique la Reserva de Inventario en tiempo real.
11. As a usuario, I want a recibir un error si una Variante de mi Carrito se agotó antes de presionar comprar, so that no pague por algo que no me van a poder entregar.
12. As a usuario, I want a poder revisar mi Historial de Pedidos, so that puedo ver los detalles de compras anteriores.
13. As a usuario, I want a ver el Estado del Pedido en mi historial, so that puedo saber si está Pendiente, Preparando, Listo para retiro, En tránsito o Entregado.

## Implementation Decisions

- **Modelo de Inventario**: El stock no se asocia al `Producto` base sino a la `Variante` (combinación de talla y color).
- **Validación de Carrito**: El carrito no reserva stock. La Reserva de Inventario es estricta y ocurre únicamente al confirmar el Pedido.
- **Costos**: El `Total` es siempre igual al `Subtotal`. No hay impuestos adicionales y el envío a domicilio es gratuito si se supera la barrera de $50.
- **Entidad Dirección**: La `Dirección de Envío` se guarda como una propiedad del `Pedido` en la base de datos, no como un campo fijo del `Usuario`.
- **Estados de Pedido**: Se dividen en estados comunes (`Pendiente`, `Preparando`, `Entregado`) y estados específicos de la modalidad de entrega (`Listo para retiro`, `En tránsito`).
- **Autenticación**: Se usará un proveedor de identidad de terceros (BaaS, ej. Firebase Auth). El backend solo guardará el identificador (UID) para relacionarlo con los pedidos.
- **Cancelaciones**: Fuera del alcance. Una vez confirmado un Pedido, su estado solo avanza.

## Testing Decisions

Se implementarán pruebas en tres capas priorizando comportamiento externo sobre detalles de implementación:
- **Capa Lógica (Pruebas Unitarias)**: Testear funciones de cálculo del carrito, validación del umbral de $50 y estructura de datos del pedido.
- **Capa de Integración**: Pruebas sobre la API backend para verificar condiciones de carrera al reservar inventario simultáneamente.
- **Capa UI/Componentes**: Testear que los botones de "Envío a domicilio" se inhabilitan si el subtotal es < $50, y que el botón de pagar muestra error si el backend reporta falta de stock.

## Out of Scope

- Pasarelas de pago reales (se asume flujo de confirmación directa o mock).
- Funcionalidad de cancelación de pedidos por parte del cliente en la aplicación.
- Cálculo de impuestos variables o costos de envío dinámicos por peso/distancia.
- Edición de una libreta de direcciones globales (Address Book) en el Perfil.

## Further Notes

- El desarrollo se dividirá en 8 fases, comenzando con el modelo de base de datos y diseño UI en Figma, pasando luego al Backend y finalmente la integración con la app móvil.
