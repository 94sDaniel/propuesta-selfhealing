package com.example.utils;

import net.serenitybdd.core.Serenity;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Localizador autocurativo muy sencillo: intenta el locator principal y,
 * si falla, recurre al locator de respaldo. Serenidad registra los pasos
 * para que quede evidencia del "Plan B".
 */
public class SmartFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartFinder.class);

    private final WebDriver driver;

    public SmartFinder(WebDriver driver) {
        this.driver = driver;
    }

    public WebElement find(By primaryLocator, By fallbackLocator) {
        try {
            WebElement element = driver.findElement(primaryLocator);
            Serenity.recordReportData().withTitle("SmartFinder").andContents("Encontré el elemento con el locator principal: " + primaryLocator);
            return element;
        } catch (NoSuchElementException primaryFailure) {
            LOGGER.warn("Locator principal no funcionó ({}). Activando Plan B...", primaryLocator, primaryFailure);
            try {
                WebElement element = driver.findElement(fallbackLocator);
                Serenity.recordReportData()
                        .withTitle("SmartFinder - Plan B")
                        .andContents("Se usó el locator de respaldo: " + fallbackLocator);
                return element;
            } catch (NoSuchElementException fallbackFailure) {
                throw new NoSuchElementException(
                        "No se encontró el elemento con ninguno de los locators. Plan B también falló.",
                        fallbackFailure
                );
            }
        }
    }
}
