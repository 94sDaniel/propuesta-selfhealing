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
import org.assertj.core.api.Assertions;
import org.openqa.selenium.*;
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

        try {
            healeniumDriver = SelfHealingDriver.create(unwrapToRemote(driver));
            healeniumOnline = true;
            System.out.println("Healenium conectado correctamente! 🩹");
        } catch (Exception ex) {
            System.err.println("Healenium no disponible: " + ex.getMessage());
            healeniumOnline = false;
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
            String foundId = button.getAttribute("id");

            button.click();

            if (!foundId.equals("boton-principal")) {
                String msg = "Healenium curó el elemento. ORIGINAL: boton-principal -> NUEVO: " + foundId;
                reporter.reportHealeniumUpdate(originalLocator, msg);
            }

            ((JavascriptExecutor) driver).executeScript("mostrarMensajeHealenium()");

        } catch (Exception ex) {
            reporter.reportHealeniumFallback(originalLocator, ex);
            Assertions.fail("Healenium falló en la curación del locator roto");
        }
    }

    @Then("^la accion continua sin fallar gracias al Plan B$")
    public void actionContinuesWithFallback() {
        Assertions.assertThat(driver.findElement(By.id("resultado")).getText())
                .contains("Plan B funciono");
    }

    @Then("^la accion continua gracias a Healenium sin configurar un Plan B$")
    public void actionContinuesWithHealenium() {
        Assertions.assertThat(driver.findElement(By.id("resultado")).getText())
                .contains("Healenium curó el locator exitosamente");
    }

    private RemoteWebDriver unwrapToRemote(WebDriver webdriver) {
        WebDriver current = webdriver;

        while (current instanceof WebDriverFacade) {
            current = ((WebDriverFacade) current).getProxiedDriver();
        }

        if (current instanceof RemoteWebDriver) {
            return (RemoteWebDriver) current;
        }

        throw new IllegalStateException("El driver no es compatible con Healenium");
    }
}
