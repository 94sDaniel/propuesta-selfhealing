package com.example.stepdefinitions;

import com.example.utils.SmartFinder;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.annotations.Managed;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SelfHealingStepDefinitions {

    @Managed(driver = "chrome")
    WebDriver driver;

    private SmartFinder smartFinder;

    @Before
    public void setUp() {
        smartFinder = new SmartFinder(driver);
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

    @Then("^la accion continua sin fallar gracias al Plan B$")
    public void actionContinuesWithFallback() {
        WebElement message = driver.findElement(By.id("resultado"));
        Assertions.assertThat(message.getText())
                .as("El mensaje de resultado deberia reflejar el clic")
                .contains("Plan B funciono");
    }
}
