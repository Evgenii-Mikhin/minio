package com.example.at.logging;

import com.example.at.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.Locale;

/**
 * Owns activation of the Chrome Side Panel log panel for the current test run.
 */
public final class LogPanelController {

    static final By SIDE_PANEL_TRIGGER = By.id("at-log-panel-sidepanel-trigger");
    static final By SIDE_PANEL_TRIGGER_FRAME = By.id("at-log-panel-sidepanel-trigger-frame");

    private LogPanelController() {
    }

    public static void openIfNeeded(WebDriver driver, String url) {
        if (!TestConfig.logPanelEnabled() || BrowserLogSink.isRemoteReady() || !isWebUrl(url)) {
            return;
        }

        openChromeSidePanel(driver);
        BrowserLogSink.activatePanel();
    }

    private static void openChromeSidePanel(WebDriver driver) {
        LogPanelServer.ensureStarted();

        clickContentScriptTrigger(driver);
        clickExtensionTrigger(driver);
        if (!openWithNativeShortcut()) {
            openWithWebDriverShortcut(driver);
        }
    }

    private static boolean clickContentScriptTrigger(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.elementToBeClickable(SIDE_PANEL_TRIGGER)).click();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean openWithNativeShortcut() {
        try {
            Robot robot = new Robot();
            robot.setAutoDelay(40);
            if (isMac()) {
                pressShortcut(robot, KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_Y);
            } else {
                pressShortcut(robot, KeyEvent.VK_ALT, KeyEvent.VK_SHIFT, KeyEvent.VK_L);
            }
            return true;
        } catch (AWTException | SecurityException e) {
            return false;
        }
    }

    private static void pressShortcut(Robot robot, int modifier, int shift, int key) {
        robot.keyPress(modifier);
        robot.keyPress(shift);
        robot.keyPress(key);
        robot.keyRelease(key);
        robot.keyRelease(shift);
        robot.keyRelease(modifier);
    }

    private static void openWithWebDriverShortcut(WebDriver driver) {
        Actions actions = new Actions(driver);
        if (isMac()) {
            actions.keyDown(Keys.COMMAND)
                    .keyDown(Keys.SHIFT)
                    .sendKeys("y")
                    .keyUp(Keys.SHIFT)
                    .keyUp(Keys.COMMAND)
                    .perform();
            return;
        }

        actions.keyDown(Keys.ALT)
                .keyDown(Keys.SHIFT)
                .sendKeys("l")
                .keyUp(Keys.SHIFT)
                .keyUp(Keys.ALT)
                .perform();
    }

    private static boolean clickExtensionTrigger(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(SIDE_PANEL_TRIGGER_FRAME));
            wait.until(ExpectedConditions.elementToBeClickable(SIDE_PANEL_TRIGGER)).click();
            return true;
        } catch (RuntimeException e) {
            return false;
        } finally {
            driver.switchTo().defaultContent();
        }
    }

    private static boolean isWebUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private static boolean isMac() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
    }
}
