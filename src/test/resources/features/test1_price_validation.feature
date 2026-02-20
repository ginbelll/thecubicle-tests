# language: es
Característica: Validación de precios en el carrito
  Como cliente de TheCubicle
  Quiero poder verificar que los precios totales del carrito sean correctos
  Para asegurarme de que no haya errores en mis compras

  Escenario: Validar el subtotal del carrito con productos seleccionados
    Dado que estoy en la pagina de inicio de TheCubicle
    Cuando selecciono 3 productos aleatorios de la pagina
    Y agrego cada producto al carrito
    Entonces el subtotal del carrito deberia ser igual a la suma de los precios de los 3 productos