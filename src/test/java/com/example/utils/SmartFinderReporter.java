package com.example.utils;

import net.serenitybdd.core.Serenity;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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

    public void reportHealeniumUpdate(By originalLocator, String healedLocator) {
        if (healedLocator != null) {
            LOGGER.info("Healenium actualizó el locator de {} a {}", originalLocator, healedLocator);
            Serenity.recordReportData()
                    .withTitle("Healenium - locator curado")
                    .andContents("Locator original: " + originalLocator + "\nLocator nuevo: " + healedLocator);
        } else {
            LOGGER.info("Healenium no necesitó curar el locator {}", originalLocator);
            Serenity.recordReportData()
                    .withTitle("Healenium - sin curación")
                    .andContents("El locator original funcionó: " + originalLocator);
        }
        attachScreenshot("Healenium - evidencia");
    }

    private void attachScreenshot(String title) {
        try {
            File screenshot = Serenity.takeScreenshot();
            if (screenshot != null && screenshot.exists()) {
                Serenity.recordReportData()
                        .withTitle(title)
                        .downloadable()
                        .fromFile(screenshot);
            }
        } catch (Exception e) {
            LOGGER.debug("No se pudo adjuntar captura para {}", title, e);
        }
    }
}
