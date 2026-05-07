package com.example.at.base;

import com.example.at.config.TestConfig;
import com.example.at.driver.DriverFactory;
import com.example.at.logging.BrowserLogSink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all UI tests. Owns WebDriver lifecycle per test method
 * (fresh, isolated browser instance for each @Test).
 */
public abstract class BaseTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    void setUp() {
        BrowserLogSink.startSession();
        log.info(
                "Starting {} browser (headless={}, mobile={}, logPanel={})",
                TestConfig.browser(),
                TestConfig.headless(),
                TestConfig.chromeMobileEnabled(),
                TestConfig.logPanelEnabled()
        );
        driver = DriverFactory.create();
        wait = new WebDriverWait(driver, TestConfig.explicitWait());
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                log.warn("Error while quitting driver", e);
            }
        }
        BrowserLogSink.reset();
    }
}
