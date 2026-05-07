package com.example.at.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * Loads test configuration from config.properties with overrides from
 * JVM system properties (-Dkey=value) and environment variables.
 */
public final class TestConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPS = load();

    private TestConfig() {
    }

    public static String browser() {
        return get("browser", "chrome");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "false"));
    }

    public static boolean logPanelEnabled() {
        return Boolean.parseBoolean(get("log.panel.enabled", "false"));
    }

    public static int logPanelBufferSize() {
        return Integer.parseInt(get("log.panel.buffer.size", "500"));
    }

    public static int logPanelServerPort() {
        return Integer.parseInt(get("log.panel.server.port", "17654"));
    }

    public static String logPanelServerBindAddress() {
        return get("log.panel.server.bind.address", "127.0.0.1");
    }

    public static String logPanelClientOrigin() {
        return get("log.panel.client.origin", "http://127.0.0.1:" + logPanelServerPort());
    }

    public static String logPanelOpenCommand() {
        return get("log.panel.open.command", "");
    }

    public static Duration logPanelOpenCommandTimeout() {
        return Duration.ofSeconds(Long.parseLong(get("log.panel.open.command.timeout.seconds", "5")));
    }

    public static String chromeBinaryPath() {
        return get("chrome.binary.path", "");
    }

    public static String chromeExtensionPath() {
        return get("chrome.extension.path", "");
    }

    public static String seleniumRemoteUrl() {
        return get("selenium.remote.url", "");
    }

    public static boolean seleniumRemoteEnabled() {
        return !seleniumRemoteUrl().isBlank();
    }

    public static String seleniumSessionName() {
        return get("selenium.session.name", "AT Framework test");
    }

    public static boolean seleniumRecordVideo() {
        return Boolean.parseBoolean(get("selenium.record.video", "false"));
    }

    public static boolean chromeMobileEnabled() {
        return Boolean.parseBoolean(get("chrome.mobile.enabled", "false"));
    }

    public static int chromeMobileWidth() {
        return Integer.parseInt(get("chrome.mobile.width", "390"));
    }

    public static int chromeMobileHeight() {
        return Integer.parseInt(get("chrome.mobile.height", "844"));
    }

    public static double chromeMobilePixelRatio() {
        return Double.parseDouble(get("chrome.mobile.pixel.ratio", "3.0"));
    }

    public static boolean chromeMobileTouch() {
        return Boolean.parseBoolean(get("chrome.mobile.touch", "true"));
    }

    public static String chromeMobilePlatform() {
        return get("chrome.mobile.platform", "Android");
    }

    public static String chromeMobileUserAgent() {
        return get("chrome.mobile.user.agent", "");
    }

    public static int windowX() {
        return Integer.parseInt(get("browser.window.x", "40"));
    }

    public static int windowY() {
        return Integer.parseInt(get("browser.window.y", "80"));
    }

    public static int windowWidth() {
        return Integer.parseInt(get("browser.window.width", "1400"));
    }

    public static int windowHeight() {
        return Integer.parseInt(get("browser.window.height", "1100"));
    }

    public static String baseUrl() {
        return get("base.url", "https://duckduckgo.com/");
    }

    public static Duration explicitWait() {
        return Duration.ofSeconds(Long.parseLong(get("explicit.wait.seconds", "10")));
    }

    public static Duration pageLoadTimeout() {
        return Duration.ofSeconds(Long.parseLong(get("page.load.timeout.seconds", "30")));
    }

    /**
     * Lookup order: system property → env var (UPPER_SNAKE) → properties file → default.
     */
    public static String get(String key, String defaultValue) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) {
            return sys;
        }
        String env = System.getenv(key.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) {
            return env;
        }
        return PROPS.getProperty(key, defaultValue);
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream stream = TestConfig.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
        return properties;
    }
}
