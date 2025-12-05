package com.example.stepdefinitions;

import com.example.utils.SmartFinder;
import com.example.utils.SmartFinderReporter;
import com.epam.healenium.SelfHealingDriver;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.annotations.Managed;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
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

    @Before
    public void setUp() {
        smartFinder = new SmartFinder(driver);
        healeniumDriver = SelfHealingDriver.create(unwrapToRemote(driver));
    }

    @Given("^el usuario abre la pagina de ejemplo de autocuracion$")
    public void openSamplePage() {
        Path html = Paths.get("src/test/resources/site/index.html").toAbsolutePath();
        driver.get(html.toUri().toString());
    }

    @When("^el usuario busca el boton con SmartFinder$")
    public void userFindsButtonWithSmartFinder() {
        WebElement button = smartFinder.find(
                // Locator inicial fallido para simular la cura
                By.id("boton-inexistente"),
                // Plan B: atributo estable preparado
                By.cssSelector("button[data-testid='boton-principal']")
        );
        button.click();
    }

    @When("^el usuario busca el boton con Healenium$")
    public void userFindsButtonWithHealenium() {
        By originalLocator = By.id("boton-inexistente");
        WebElement button = healeniumDriver.findElement(originalLocator);
        reportHealingResult(originalLocator);
        button.click();
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
                .contains("Plan B funciono");
    }

    private void reportHealingResult(By originalLocator) {
        String healedLocator = null;
        try {
            Object healingResult = healeniumDriver.getClass().getMethod("getLastHealingResult").invoke(healeniumDriver);
            if (healingResult != null) {
                Object target = healingResult.getClass().getMethod("getTarget").invoke(healingResult);
                if (target != null) {
                    Object locatorValue = target.getClass().getMethod("getLocator").invoke(target);
                    healedLocator = locatorValue != null ? locatorValue.toString() : null;
                }
            }
        } catch (Exception ignored) {
            // Si Healenium cambia su API, seguimos adelante sin bloquear la prueba.
        }
        reporter.reportHealeniumUpdate(originalLocator, healedLocator);
    }

    private RemoteWebDriver unwrapToRemote(WebDriver webdriver) {
        if (webdriver instanceof WebDriverFacade) {
            WebDriver proxied = ((WebDriverFacade) webdriver).getProxiedDriver();
            if (proxied instanceof RemoteWebDriver) {
                return (RemoteWebDriver) proxied;
            }
        }
        if (webdriver instanceof RemoteWebDriver) {
            return (RemoteWebDriver) webdriver;
        }
        throw new IllegalStateException("El WebDriver administrado no es compatible con RemoteWebDriver necesario para Healenium");
    }
}
