package com.example.at.driver;

import com.example.at.config.TestConfig;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates WebDriver instances based on TestConfig.
 * Selenium Manager (built into Selenium 4.6+) resolves driver binaries automatically.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create() {
        String browser = TestConfig.browser().toLowerCase();
        boolean headless = TestConfig.headless();
        validateConfiguration(browser, headless);

        WebDriver rawDriver = TestConfig.seleniumRemoteEnabled()
                ? createRemote(browser, headless)
                : switch (browser) {
                    case "chrome" -> createChrome(headless);
                    case "firefox" -> createFirefox(headless);
                    default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
                };

        WebDriver driver = rawDriver;
        driver.manage().timeouts().pageLoadTimeout(TestConfig.pageLoadTimeout());
        driver.manage().window().setPosition(new Point(TestConfig.windowX(), TestConfig.windowY()));
        driver.manage().window().setSize(new Dimension(TestConfig.windowWidth(), TestConfig.windowHeight()));
        return driver;
    }

    private static WebDriver createChrome(boolean headless) {
        return new ChromeDriver(chromeOptions(headless));
    }

    private static WebDriver createRemote(String browser, boolean headless) {
        try {
            return switch (browser) {
                case "chrome" -> new RemoteWebDriver(new URL(TestConfig.seleniumRemoteUrl()), chromeOptions(headless));
                case "firefox" -> new RemoteWebDriver(new URL(TestConfig.seleniumRemoteUrl()), firefoxOptions(headless));
                default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
            };
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Selenium Remote URL: " + TestConfig.seleniumRemoteUrl(), e);
        }
    }

    static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        if (TestConfig.chromeMobileEnabled()) {
            options.setExperimentalOption("mobileEmulation", mobileEmulationOptions());
        }
        if (TestConfig.logPanelEnabled() && !headless) {
            if (!TestConfig.seleniumRemoteEnabled()) {
                options.setBinary(ChromeBinaryResolver.requireSidePanelBrowserBinary().toFile());
            }
            String extensionDir = chromeExtensionDir();
            options.addArguments(
                    "--disable-extensions-except=" + extensionDir,
                    "--load-extension=" + extensionDir
            );
            options.setExperimentalOption("excludeSwitches", List.of("test-type"));
        }
        if (TestConfig.seleniumRemoteEnabled()) {
            options.setCapability("se:name", TestConfig.seleniumSessionName());
            options.setCapability("se:recordVideo", TestConfig.seleniumRecordVideo());
        }
        options.addArguments(
                "--disable-gpu",
                "--no-sandbox",
                "--window-size=" + TestConfig.windowWidth() + "," + TestConfig.windowHeight()
        );
        return options;
    }

    private static WebDriver createFirefox(boolean headless) {
        return new FirefoxDriver(firefoxOptions(headless));
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        if (TestConfig.seleniumRemoteEnabled()) {
            options.setCapability("se:name", TestConfig.seleniumSessionName());
            options.setCapability("se:recordVideo", TestConfig.seleniumRecordVideo());
        }
        return options;
    }

    private static String chromeExtensionDir() {
        String configuredPath = TestConfig.chromeExtensionPath();
        if (!configuredPath.isBlank()) {
            return configuredPath;
        }
        return ChromeExtensionResolver.resolveUnpackedExtensionDir().toString();
    }

    private static void validateConfiguration(String browser, boolean headless) {
        if (TestConfig.logPanelEnabled() && !"chrome".equals(browser)) {
            throw new IllegalArgumentException("Chrome Side Panel log panel works only with Chrome");
        }
        if (TestConfig.logPanelEnabled() && headless) {
            throw new IllegalArgumentException("Chrome Side Panel log panel requires headed Chrome");
        }
        if (TestConfig.chromeMobileEnabled() && !"chrome".equals(browser)) {
            throw new IllegalArgumentException("Chrome mobile emulation is only supported with Chrome");
        }
    }

    private static Map<String, Object> mobileEmulationOptions() {
        Map<String, Object> deviceMetrics = new HashMap<>();
        deviceMetrics.put("width", TestConfig.chromeMobileWidth());
        deviceMetrics.put("height", TestConfig.chromeMobileHeight());
        deviceMetrics.put("pixelRatio", TestConfig.chromeMobilePixelRatio());
        deviceMetrics.put("touch", TestConfig.chromeMobileTouch());
        deviceMetrics.put("mobile", true);

        Map<String, Object> clientHints = new HashMap<>();
        clientHints.put("platform", TestConfig.chromeMobilePlatform());
        clientHints.put("mobile", true);

        Map<String, Object> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceMetrics", deviceMetrics);
        mobileEmulation.put("clientHints", clientHints);

        String userAgent = TestConfig.chromeMobileUserAgent();
        if (!userAgent.isBlank()) {
            mobileEmulation.put("userAgent", userAgent);
        }

        return mobileEmulation;
    }
}
