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

    public SmartFinder(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * SmartFinder intenta primero el locator principal.
     * Si falla, usa un fallback y reporta en Serenity.
     */
    public WebElement find(By primaryLocator, By fallbackLocator) {
        LOGGER.info("🔍 SmartFinder intentando encontrar: {}", primaryLocator);

        try {
            WebElement element = driver.findElement(primaryLocator);
            LOGGER.info("✔ Locator primario funcionó: {}", primaryLocator);

            Serenity.recordReportData()
                    .withTitle("SmartFinder - locator primario OK")
                    .andContents("Elemento encontrado con: " + primaryLocator);

            return element;

        } catch (NoSuchElementException e) {
            LOGGER.warn("❌ Locator primario falló: {}", primaryLocator);

            Serenity.recordReportData()
                    .withTitle("SmartFinder - fallback usado")
                    .andContents("Locator roto: " + primaryLocator +
                            "\nFallback elegido: " + fallbackLocator);

            LOGGER.info("➡ Reintentando con fallback: {}", fallbackLocator);

            return driver.findElement(fallbackLocator);
        }
    }
}
