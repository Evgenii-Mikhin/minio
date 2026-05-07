package com.example.at.pages;

import com.example.at.logging.BrowserLogSink;
import com.example.at.logging.LogPanelController;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Thin base for Page Objects. Holds driver + wait and common lookup helpers.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    protected void openUrl(String url) {
        BrowserLogSink.markRemoteUnavailable();
        driver.get(url);
        LogPanelController.openIfNeeded(driver, driver.getCurrentUrl());
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
}
