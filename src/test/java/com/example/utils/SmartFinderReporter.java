package com.example.utils;

import net.serenitybdd.core.Serenity;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

        boolean healed = healedLocator != null
                && !healedLocator.contains(originalLocator.toString())
                && (healedLocator.contains("By.") || healedLocator.contains("cssSelector"));

        if (healed) {
            LOGGER.info("Healenium CURÓ el locator de {} a {}", originalLocator, healedLocator);
            Serenity.recordReportData()
                    .withTitle("Healenium - locator curado 🟢")
                    .andContents(
                            "Locator original: " + originalLocator +
                                    "\nLocator curado: " + healedLocator
                    );
        } else {
            LOGGER.info("Healenium NO curó el locator {}", originalLocator);
            Serenity.recordReportData()
                    .withTitle("Healenium - sin curación ⚪")
                    .andContents(
                            "El locator original funcionó o no hubo cambio: " + originalLocator
                    );
        }

        attachScreenshot("Healenium - evidencia");
    }

    public void reportHealeniumUnavailable(Exception error) {
        LOGGER.warn("Healenium no está disponible, se usará un fallback", error);
        Serenity.recordReportData()
                .withTitle("Healenium - no disponible")
                .andContents("Se omitió Healenium porque no respondió: " + error.getMessage());
    }

    public void reportHealeniumFallback(By locator, Exception error) {
        LOGGER.warn("Healenium falló al curar {}. Se usará SmartFinder como respaldo.", locator, error);
        Serenity.recordReportData()
                .withTitle("Healenium - fallback a SmartFinder")
                .andContents("Locator original: " + locator + "\nMotivo: " + error.getMessage());
    }

    private void attachScreenshot(String title) {
        try {
            WebDriver driver = Serenity.getDriver();
            if (!(driver instanceof TakesScreenshot)) {
                LOGGER.debug("El driver no soporta capturas, se omite screenshot para {}", title);
                return;
            }

            File rawScreenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            if (rawScreenshot == null || !rawScreenshot.exists()) {
                LOGGER.debug("No se obtuvo archivo de captura para {}", title);
                return;
            }

            Path evidenceDir = Paths.get("target", "serenity-evidence");
            Files.createDirectories(evidenceDir);
            String safeTitle = title.replaceAll("[^a-zA-Z0-9-_ ]", "_").replaceAll("\\s+", "_");
            Path targetScreenshot = evidenceDir.resolve(safeTitle + ".png");
            Files.copy(rawScreenshot.toPath(), targetScreenshot, StandardCopyOption.REPLACE_EXISTING);

            Serenity.recordReportData()
                    .withTitle(title)
                    .downloadable()
                    .fromFile(targetScreenshot);
        } catch (Exception e) {
            LOGGER.debug("No se pudo adjuntar captura para {}", title, e);
        }
    }
}
