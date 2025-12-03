# Propuesta: SmartFinder con autocuración (Serenity + Cucumber)

Ejemplo mínimo para mostrar cómo un "Plan B" permite que las pruebas web sigan funcionando cuando cambia un locator. El proyecto usa **Java 11**, **Maven**, **Serenity BDD** y **Cucumber**. Incluye una página HTML local que simula el caso: el locator principal falla a propósito y `SmartFinder` activa un locator de respaldo.

## Estructura
- `pom.xml`: dependencias de Serenity, Cucumber y Selenium (incluye `selenium-devtools-v85`, el artefacto disponible en Maven Central que expone `HasDevTools` usado por Serenity 4.x).
- `src/test/resources/site/index.html`: página local con un botón y un mensaje de resultado.
- `src/test/java/com/example/utils/SmartFinder.java`: helper autocurativo.
- `src/test/java/com/example/stepdefinitions/SelfHealingStepDefinitions.java`: pasos de Cucumber que usan `SmartFinder` y el plan B.
- `src/test/java/com/example/runner/SmartFinderTest.java`: runner de Serenity+Cucumber.
- `src/test/resources/features/smartfinder/self_healing.feature`: escenario en Gherkin (español).

## Cómo ejecutar
1. Instala Java 11 y Maven 3.8+.
2. Ejecuta las pruebas con Serenity (requiere un navegador disponible, por defecto Chrome):
   ```bash
   mvn test
   ```
3. Revisa el reporte en `target/site/serenity/index.html` para ver los pasos y el log de cuando se activó el Plan B.

## Flujo autocurativo
1. El test abre `index.html` desde recursos locales.
2. Busca el botón con un locator primario intencionalmente incorrecto (`id="boton-inexistente"`).
3. `SmartFinder` captura el `NoSuchElementException` y cambia al locator de respaldo (`data-testid="boton-principal"`).
4. Hace clic y el texto de la página cambia a "Plan B funciono...", demostrando que la prueba continúa sana.
