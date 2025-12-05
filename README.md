# Propuesta: SmartFinder con autocuración (Serenity + Cucumber)

Ejemplo mínimo para mostrar cómo un "Plan B" permite que las pruebas web sigan funcionando cuando cambia un locator. El proyecto
usa **Java 11**, **Maven**, **Serenity BDD** y **Cucumber**. Incluye una página HTML local que simula el caso: el locator principal falla a propósito y `SmartFinder` activa un locator de respaldo.

## Estructura
- `pom.xml`: dependencias de Serenity y Selenium (incluye `selenium-java` y `selenium-devtools-v120`, artefacto que expone `HasDevTools` usado por Serenity 4.x) y ahora `healenium-web` para un enfoque sin Plan B manual.
- `src/test/resources/serenity.conf`: configura capturas `BEFORE_AND_AFTER_EACH_STEP` para que el reporte tenga evidencia visual.
- `src/test/resources/site/index.html`: página local con un botón y un mensaje de resultado.
- `src/test/java/com/example/utils/SmartFinder.java`: helper autocurativo que intenta locator principal y, si falla, activa el Plan B.
- `src/test/java/com/example/utils/SmartFinderReporter.java`: encapsula el registro en logs y en el reporte Serenity, adjuntando capturas al encontrar el elemento. También reporta cuando Healenium cura un locator.
- `src/test/java/com/example/stepdefinitions/SelfHealingStepDefinitions.java`: pasos de Cucumber que usan `SmartFinder` y también un flujo alterno con Healenium.
- `src/test/java/com/example/runner/SmartFinderTest.java`: runner de Serenity+Cucumber.
- `src/test/resources/features/smartfinder/self_healing.feature`: escenarios en Gherkin (español).
- `src/test/resources/healenium.properties`: configuración de Healenium (host/puerto del backend) para el flujo de curación automática.

## Cómo ejecutar
1. Instala Java 11 y Maven 3.8+.
2. Ejecuta las pruebas con Serenity (requiere un navegador disponible, por defecto Chrome):
   ```bash
   mvn test
   ```
3. Revisa el reporte en `target/site/serenity/index.html` para ver los pasos, capturas y el log de cuando se activó el Plan B.

## Flujo autocurativo
1. El test abre `index.html` desde recursos locales.
2. Busca el botón con un locator primario intencionalmente incorrecto (`id="boton-inexistente"`).
3. `SmartFinder` captura el `NoSuchElementException` y cambia al locator de respaldo (`data-testid="boton-principal"`).
4. Se registra en Serenity (con captura) cómo se encontró el elemento y se hace clic para que el texto cambie a "Plan B funciono...", demostrando que la prueba continúa sana.

## Flujo con Healenium (sin Plan B manual)
1. Usamos `SelfHealingDriver` de Healenium, que envía los locators al backend (`healenium.properties`).
2. Al no encontrar el `id="boton-inexistente"`, Healenium busca alternativas similares y devuelve un nuevo locator curado.
3. Registramos en Serenity qué locator curó Healenium (sin detener la prueba) y adjuntamos captura.
   - Nota: en Serenity el WebDriver es un `WebDriverFacade` (o `DevToolsWebDriverFacade`), por lo que lo desenrollamos hasta el `RemoteWebDriver` real antes de construir el `SelfHealingDriver` para evitar `ClassCastException`.
4. El clic se ejecuta con el locator curado y el mensaje en pantalla confirma que la prueba siguió viva.
