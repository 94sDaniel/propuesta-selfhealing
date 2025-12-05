package com.example.utils;

import net.serenitybdd.core.Serenity;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class SmartFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmartFinder.class);
    private final WebDriver driver;
    private final SmartFinderReporter reporter;
    public SmartFinder(WebDriver driver) {
        this.driver = driver;
        this.reporter = new SmartFinderReporter();
    }
    public WebElement find(By primaryLocator, By fallbackLocator) {
        try {
            WebElement element = driver.findElement(primaryLocator);
            Serenity.recordReportData().withTitle("SmartFinder").andContents("Encontré el elemento con el locator principal: " + primaryLocator);
            reporter.reportSuccess("locator principal", primaryLocator, element);
            return element;
        } catch (NoSuchElementException primaryFailure) {
            reporter.reportFailure("locator principal", primaryLocator, primaryFailure);
            LOGGER.warn("Locator principal no funcionó ({}). Activando Plan B...", primaryLocator, primaryFailure);
            try {
                WebElement element = driver.findElement(fallbackLocator);
                reporter.reportSuccess("Plan B", fallbackLocator, element);
                Serenity.recordReportData()
                        .withTitle("SmartFinder - Plan B")
                        .andContents("Se usó el locator de respaldo: " + fallbackLocator);
                return element;
            } catch (NoSuchElementException fallbackFailure) {
                reporter.reportFailure("Plan B", fallbackLocator, fallbackFailure);
                throw new NoSuchElementException(
                        "No se encontró el elemento con ninguno de los locators. Plan B también falló.",
                        fallbackFailure
                );
            }
        }
    }
}
