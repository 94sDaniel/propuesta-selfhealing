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

    // --- NUEVO MÉTODO SIMPLIFICADO (el que usas ahora) ---
    public void reportHealeniumUpdate(By originalLocator, String details) {
        LOGGER.info("📌 Healenium report: {}", details);

        Serenity.recordReportData()
                .withTitle("Healenium – con curación 🩹")
                .andContents(
                        "Locator Original roto: " + originalLocator +
                                "\nResultado: " + details
                );

        attachScreenshot("Healenium - evidencia");
    }

    public void reportHealeniumFallback(By locator, Exception error) {
        LOGGER.warn("Healenium fallback para {}. Motivo: {}", locator, error.getMessage());
        Serenity.recordReportData()
                .withTitle("Healenium – fallback a SmartFinder ⚠️")
                .andContents(
                        "Locator con fallo: " + locator +
                                "\nMotivo: " + error.getMessage()
                );
        attachScreenshot("Healenium - fallback evidencia");
    }

    private void attachScreenshot(String title) {
        try {
            WebDriver driver = Serenity.getDriver();
            if (!(driver instanceof TakesScreenshot)) {
                LOGGER.debug("El driver no soporta capturas: {}", title);
                return;
            }

            File rawScreenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            if (rawScreenshot == null || !rawScreenshot.exists()) return;

            Path evidenceDir = Paths.get("target", "serenity-evidence");
            Files.createDirectories(evidenceDir);

            String safeTitle = title.replaceAll("[^a-zA-Z0-9-_ ]", "_")
                    .replaceAll("\\s+", "_");

            Path targetScreenshot = evidenceDir.resolve(safeTitle + ".png");

            Files.copy(rawScreenshot.toPath(), targetScreenshot, StandardCopyOption.REPLACE_EXISTING);

            Serenity.recordReportData()
                    .withTitle(title)
                    .downloadable()
                    .fromFile(targetScreenshot);

        } catch (Exception e) {
            LOGGER.debug("No se pudo adjuntar screenshot para {}", title, e);
        }
    }
}
