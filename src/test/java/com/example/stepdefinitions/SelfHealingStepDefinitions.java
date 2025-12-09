package com.example.stepdefinitions;

import com.example.utils.SmartFinder;
import com.example.utils.SmartFinderReporter;
import com.epam.healenium.SelfHealingDriver;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.annotations.Managed;
import net.serenitybdd.core.Serenity;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import net.thucydides.core.webdriver.WebDriverFacade;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SelfHealingStepDefinitions {

    @Managed(driver = "chrome")
    WebDriver driver;

    private SmartFinder smartFinder;
    private SelfHealingDriver healeniumDriver;
    private final SmartFinderReporter reporter = new SmartFinderReporter();
    private boolean healeniumOnline = false;

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        smartFinder = new SmartFinder(driver);
        int maxAttempts = 5;  // Número máximo de intentos
        long delayMs = 3000;  // Espera de 3 segundos entre intentos

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                // Intento de crear el Healenium Driver
                System.out.println(String.format("Intentando conectar con Healenium (Intento %d/%d)...", attempt, maxAttempts));
                healeniumDriver = SelfHealingDriver.create(unwrapToRemote(driver));
                healeniumOnline = true;
                // Si tiene éxito, salimos del bucle
                System.out.println("¡Conexión exitosa con Healenium!");
                return;
            } catch (Exception ex) {
                if (attempt < maxAttempts) {
                    // Si falla y aún quedan intentos, esperamos y reintentamos
                    System.out.println(String.format("Fallo al conectar. Esperando %dms para reintentar...", delayMs));
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // Si es el último intento y falla, se reporta el error final.
                    reporter.reportHealeniumUnavailable(ex);
                    healeniumOnline = false;
                }
            }
        }
    }

    @Given("^el usuario abre la pagina de ejemplo de autocuracion$")
    public void openSamplePage() {
        Path html = Paths.get("src/test/resources/site/index.html").toAbsolutePath();
        driver.get(html.toUri().toString());
    }

    @When("^el usuario busca el boton con SmartFinder$")
    public void userFindsButtonWithSmartFinder() {
        WebElement button = smartFinder.find(
                By.id("boton-inexistente"),
                By.cssSelector("button[data-testid='boton-principal']")
        );
        button.click();
    }

    @When("^el usuario busca el boton con Healenium$")
    public void userFindsButtonWithHealenium() {
        By originalLocator = By.id("boton-principal");
        try {
            WebElement button = healeniumDriver.findElement(originalLocator);
            button.click();
            reportHealingResult(originalLocator);
            ((JavascriptExecutor) driver).executeScript("mostrarMensajeHealenium()");
        } catch (Exception ex) {
            reporter.reportHealeniumFallback(originalLocator, ex);
            Assertions.fail("Healenium no logró curar el locator roto");
        }
    }

    @Then("^la accion continua sin fallar gracias al Plan B$")
    public void actionContinuesWithFallback() {
        WebElement message = driver.findElement(By.id("resultado"));
        Assertions.assertThat(message.getText())
                .as("El mensaje de resultado deberia reflejar el clic")
                .contains("Plan B funciono");
    }

    @Then("^la accion continua gracias a Healenium sin configurar un Plan B$")
    public void actionContinuesWithHealenium() {
        WebElement message = driver.findElement(By.id("resultado"));
        Assertions.assertThat(message.getText())
                .as("El mensaje de resultado deberia reflejar el clic curado")
                .contains("Healenium curó el locator exitosamente");
    }
    private void reportHealingResult(By originalLocator) {
        String healedLocator = null;
        boolean isHealed = false;
        Double score = null;

        try {
            Object healingResult = healeniumDriver.getClass().getMethod("getLastHealingResult").invoke(healeniumDriver);
            if (healingResult != null) {

                Object scoreObj = healingResult.getClass().getMethod("getScore").invoke(healingResult);
                if (scoreObj instanceof Number) {
                    score = ((Number) scoreObj).doubleValue();
                }

                Object target = healingResult.getClass().getMethod("getTarget").invoke(healingResult);
                if (target != null) {
                    Object locatorObj = target.getClass().getMethod("getLocator").invoke(target);
                    if (locatorObj != null) {
                        healedLocator = locatorObj.toString();
                        isHealed = !healedLocator.equals(originalLocator.toString());
                    }
                }
            }
        } catch (Exception ignored) {}

        if (isHealed) {
            reporter.reportHealeniumUpdate(originalLocator,
                    healedLocator + " | Score: " + score);
        } else {
            reporter.reportHealeniumUpdate(originalLocator, null);
        }
    }

    private RemoteWebDriver unwrapToRemote(WebDriver webdriver) {
        WebDriver current = webdriver;

        while (current instanceof WebDriverFacade) {
            WebDriver proxied = ((WebDriverFacade) current).getProxiedDriver();
            // DevToolsWebDriverFacade también es un WebDriverFacade, así que seguimos desenrollando
            if (proxied == current) {
                break;
            }
            current = proxied;
        }

        if (current instanceof RemoteWebDriver) {
            return (RemoteWebDriver) current;
        }

        throw new IllegalStateException("El WebDriver administrado no es compatible con RemoteWebDriver necesario para Healenium");
    }
}
