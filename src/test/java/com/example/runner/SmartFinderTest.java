package com.example.runner;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/smartfinder",
        glue = "com.example.stepdefinitions",
        plugin = {"pretty"},
        tags = "@healing",
        snippets = CucumberOptions.SnippetType.CAMELCASE
)
public class SmartFinderTest {
}
