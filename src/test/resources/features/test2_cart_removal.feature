# language: es
Característica: Eliminación de productos del carrito
  Como cliente de TheCubicle
  Quiero poder eliminar productos de mi carrito de compras
  Para mantener mi carrito organizado y solo con los productos que deseo comprar

  Escenario: Eliminar 3 productos y validar que queden los 2 correctos
  Dado que estoy en la pagina de inicio de TheCubicle
  Cuando selecciono 5 productos aleatorios de la pagina
  Y agrego todos los 5 productos al carrito
  Y elijo al azar 3 productos para remover del carrito
  Y remuevo los 3 productos del carrito
  Entonces solo 2 productos deberian permanecer en el carrito
  Y los 2 productos restantes deberian coincidir con los que no fueron removidos
