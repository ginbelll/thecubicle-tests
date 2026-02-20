# language: es
Característica: Ordenamiento alfabetico de productos por marca
  Como cliente de TheCubicle
  Quiero poder ordenar los productos por marca
  Para encontrar fácilmente los productos de mi marca favorita por nombre

  Escenario: Validar la organizacion alfabetica de los productos de una marca especifica
  Dado que estoy en la pagina de marcas populares de TheCubicle
  Cuando selecciono una marca aleatoria de la pagina
  Y navego a la pagina de productos de esa marca
  Y ordeno los productos alfabeticamente de la A a la Z
  Entonces todos los productos en la primera pagina deberian estar en orden alfabetico
  