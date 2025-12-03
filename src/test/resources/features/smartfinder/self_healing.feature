# language: es
Característica: Autocuración de locators con SmartFinder
  Para evitar que las pruebas se rompan por cambios menores en la UI
  Como tester automatizador
  Quiero que el flujo use un locator de respaldo cuando el principal falle

  Escenario: SmartFinder activa el Plan B y continua la prueba
    Dado el usuario abre la pagina de ejemplo de autocuracion
    Cuando el usuario busca el boton con SmartFinder
    Entonces la accion continua sin fallar gracias al Plan B
