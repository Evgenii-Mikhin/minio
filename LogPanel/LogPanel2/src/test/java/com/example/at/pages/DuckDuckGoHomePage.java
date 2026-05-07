package com.example.at.pages;

import com.example.at.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DuckDuckGoHomePage extends BasePage {

    private static final By SEARCH_INPUT = By.cssSelector("input[name='q']");

    public DuckDuckGoHomePage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public DuckDuckGoHomePage open() {
        openUrl(TestConfig.baseUrl());
        waitVisible(SEARCH_INPUT);
        return this;
    }

    public DuckDuckGoSearchResultsPage searchFor(String query) {
        waitVisible(SEARCH_INPUT).sendKeys(query, Keys.ENTER);
        return new DuckDuckGoSearchResultsPage(driver, wait);
    }
}
