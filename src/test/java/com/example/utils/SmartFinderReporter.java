package com.example.utils;

import net.serenitybdd.core.Serenity;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path; // Se mantiene el import para Path
import org.openqa.selenium.TakesScreenshot; // <<<< NECESARIO AÑADIR
import org.openqa.selenium.OutputType; // <<<< NECESARIO AÑADIR

/**
 * Encapsula la forma en que SmartFinder reporta cómo se encontró el elemento.
 * Registra en logs y adjunta evidencia al reporte de Serenity (incluyendo capturas).
 */
public class SmartFinderReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartFinderReporter.class);

    public void reportSuccess(String strategyLabel, By locator, WebElement element) {
        LOGGER.info("SmartFinder encontró el elemento usando {}: {}", strategyLabel, locator);
        Serenity.recordReportData()
                .withTitle("SmartFinder - " + strategyLabel)
                .andContents("Locator utilizado: " + locator + "\nTag: " + element.getTagName());
        attachScreenshot("SmartFinder - " + strategyLabel + " (captura)");
    }

    public void reportFailure(String strategyLabel, By locator, Exception error) {
        LOGGER.warn("SmartFinder no encontró el elemento usando {}: {}", strategyLabel, locator, error);
        Serenity.recordReportData()
                .withTitle("SmartFinder - fallo de " + strategyLabel)
                .andContents("No se encontró el elemento con el locator: " + locator);
    }

    private void attachScreenshot(String title) {
        try {
            // FIX: Accedemos al WebDriver gestionado por Serenity y usamos la API estándar de Selenium para obtener el archivo.
            File screenshotFile = ((TakesScreenshot) Serenity.getWebdriverManager().getWebdriver()).getScreenshotAs(OutputType.FILE);

            if (screenshotFile != null && screenshotFile.exists()) {
                Serenity.recordReportData()
                        .withTitle(title)
                        .downloadable()
                        // FIX: Convertimos File a Path, que es lo que Serenity 4.x espera en fromFile()
                        .fromFile(screenshotFile.toPath());
            }
        } catch (Exception e) {
            LOGGER.debug("No se pudo adjuntar captura para {}", title, e);
        }
    }
}