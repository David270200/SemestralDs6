# Asistente Planificador (Tienda de Ropa)

Aplicación móvil para una tienda de ropa que incluye catálogo de productos, carrito de compras y gestión de pedidos.

## Language

**Pedido**:
La solicitud formal de compra de uno o más productos. Solo se crea si hay stock disponible para todos los productos en el carrito.
_Avoid_: Compra, transacción

**Carrito**:
La selección temporal de productos que un usuario desea comprar. Se utiliza para calcular subtotales y validar métodos de entrega antes de convertirse en un Pedido.
_Avoid_: Canasta

**Retiro en tienda**:
Método de entrega disponible para todos los pedidos, sin importar su monto.
_Avoid_: Recogida, pick-up

**Envío a domicilio**:
Método de entrega restringido, disponible únicamente para pedidos cuyo total es igual o superior a $50.
_Avoid_: Delivery, envío normal

**Producto**:
El modelo general de una prenda de ropa (ej. "Camisa Polo"). No tiene stock directamente asociado.
_Avoid_: Artículo, ítem

**Variante**:
Una versión específica de un Producto, definida por una combinación única de talla y color. El control de stock disponible y las adiciones al Carrito se realizan a nivel de Variante.
_Avoid_: SKU, modelo específico

**Estado del Pedido**:
La fase logística en la que se encuentra un Pedido. Ambos métodos de entrega comparten los estados `Pendiente`, `Preparando` y `Entregado`. Para el retiro en tienda existe el estado exclusivo `Listo para retiro`, y para el envío existe `En tránsito`.
_Avoid_: Estatus, situación

**Subtotal**:
La suma del precio de todas las Variantes en el Carrito o Pedido.
_Avoid_: Suma de productos

**Total**:
El valor final a pagar. En este dominio específico, el Total siempre es idéntico al Subtotal, ya que el envío a domicilio es gratuito y no se modelan impuestos adicionales.
_Avoid_: Gran total

**Dirección de Envío**:
La ubicación física a la cual se enviará un Pedido que utiliza la modalidad de Envío a domicilio. Pertenece al Pedido, lo que permite que un mismo usuario envíe diferentes pedidos a diferentes ubicaciones.
_Avoid_: Dirección del usuario, domicilio del perfil

**Reserva de Inventario**:
El acto de descontar el stock disponible de una Variante en la base de datos. Se ejecuta de forma estricta únicamente en el instante en que se confirma la creación del Pedido, y nunca mientras los productos permanecen en el Carrito.
_Avoid_: Bloqueo de stock temporal

**Usuario**:
La persona que utiliza la aplicación para realizar Pedidos. Su identidad es gestionada por un proveedor de terceros (ej. Firebase o Google Sign-In), por lo que el sistema no almacena ni maneja contraseñas directamente; únicamente vincula el identificador externo con el historial de compras.
_Avoid_: Cuenta con contraseña
