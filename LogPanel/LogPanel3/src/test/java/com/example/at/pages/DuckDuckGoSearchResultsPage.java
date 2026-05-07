package com.example.at.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DuckDuckGoSearchResultsPage extends BasePage {

    private static final By MAINLINE = By.cssSelector("[data-testid='mainline'], #react-layout");
    private static final By SEARCH_INPUT = By.cssSelector("input[name='q']");

    public DuckDuckGoSearchResultsPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public DuckDuckGoSearchResultsPage waitUntilLoaded() {
        wait.until(ExpectedConditions.presenceOfElementLocated(MAINLINE));
        wait.until(d -> {
            String value = d.findElement(SEARCH_INPUT).getDomProperty("value");
            return value != null && !value.isBlank();
        });
        return this;
    }

    public boolean hasResultsLayout() {
        return !driver.findElements(MAINLINE).isEmpty();
    }

    public DuckDuckGoSearchResultsPage replaceQuery(String query) {
        scrollToTop();
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
                input
        );
        input.click();
        clearSearchInput(input);
        input.sendKeys(query, Keys.ENTER);
        return waitUntilLoaded();
    }

    public DuckDuckGoSearchResultsPage scrollBy(int pixels) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, arguments[0]);", pixels);
        return this;
    }

    public DuckDuckGoSearchResultsPage scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        return this;
    }

    public long scrollY() {
        Object value = ((JavascriptExecutor) driver).executeScript("return Math.round(window.scrollY);");
        return value instanceof Number number ? number.longValue() : 0L;
    }

    public String searchQuery() {
        return waitVisible(SEARCH_INPUT).getDomProperty("value");
    }

    public String pageTitle() {
        return driver.getTitle();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    private void clearSearchInput(WebElement input) {
        ((JavascriptExecutor) driver).executeScript("""
                const input = arguments[0];
                const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
                setter.call(input, '');
                input.dispatchEvent(new Event('input', { bubbles: true }));
                input.dispatchEvent(new Event('change', { bubbles: true }));
                """, input);
    }
}
