package com.example.at.driver;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverFactoryTest {

    @Test
    void chromeOptionsEnableMobileEmulationAndLoadSidePanelExtensionByDefault() {
        ChromeOptions options = DriverFactory.chromeOptions(false);

        Map<String, Object> chromeOptions = chromeOptions(options);
        String binary = (String) chromeOptions.get("binary");
        @SuppressWarnings("unchecked")
        Map<String, Object> mobileEmulation = (Map<String, Object>) chromeOptions.get("mobileEmulation");
        @SuppressWarnings("unchecked")
        Map<String, Object> deviceMetrics = (Map<String, Object>) mobileEmulation.get("deviceMetrics");

        assertEquals(390, deviceMetrics.get("width"));
        assertEquals(844, deviceMetrics.get("height"));
        assertEquals(true, deviceMetrics.get("mobile"));
        assertNotNull(binary, "Chrome Side Panel tests must run in a browser binary that accepts test extensions");
        assertTrue(binary.toLowerCase(Locale.ROOT).contains("chrome")
                || binary.toLowerCase(Locale.ROOT).contains("chromium"));

        @SuppressWarnings("unchecked")
        List<String> args = (List<String>) chromeOptions.get("args");
        @SuppressWarnings("unchecked")
        List<String> excludedSwitches = (List<String>) chromeOptions.getOrDefault("excludeSwitches", List.of());

        assertTrue(excludedSwitches.contains("test-type"));
        assertTrue(hasExtensionArgument(args, "--load-extension="));
        assertTrue(hasExtensionArgument(args, "--disable-extensions-except="));
        assertFalse(args.stream().anyMatch(arg -> arg.startsWith("--user-data-dir=")));
    }

    @Test
    void bundledExtensionDeclaresChromeSidePanelApi() throws Exception {
        URL manifestUrl = Thread.currentThread()
                .getContextClassLoader()
                .getResource("extensions/aut-logs-sidepanel/manifest.json");

        assertNotNull(manifestUrl, "Side panel extension manifest must be bundled");

        Path manifestPath = Path.of(manifestUrl.toURI());
        Path extensionDir = manifestPath.getParent();
        String manifest = Files.readString(manifestPath);

        assertTrue(manifest.contains("\"manifest_version\": 3"));
        assertTrue(manifest.contains("\"sidePanel\""));
        assertTrue(manifest.contains("\"tabs\""));
        assertTrue(manifest.contains("\"side_panel\""));
        assertTrue(manifest.contains("\"default_path\": \"sidepanel.html\""));
        assertTrue(manifest.contains("\"content_scripts\""));
        assertTrue(manifest.contains("\"content-script.js\""));
        assertTrue(manifest.contains("\"web_accessible_resources\""));
        assertTrue(manifest.contains("\"trigger.html\""));
        assertTrue(manifest.contains("\"_execute_action\""));
        assertTrue(manifest.contains("\"default\": \"Alt+Shift+L\""));
        assertTrue(manifest.contains("\"mac\": \"Command+Shift+Y\""));

        String serviceWorker = Files.readString(extensionDir.resolve("service-worker.js"));
        String contentScript = Files.readString(extensionDir.resolve("content-script.js"));
        assertTrue(serviceWorker.contains("chrome.action.onClicked.addListener"));
        assertTrue(serviceWorker.contains("chrome.runtime.onMessage.addListener"));
        assertTrue(serviceWorker.contains("chrome.sidePanel.open"));
        assertTrue(contentScript.contains("chrome.runtime.sendMessage"));
        assertTrue(contentScript.contains("OPEN_AT_LOG_PANEL"));

        assertTrue(Files.isRegularFile(extensionDir.resolve("service-worker.js")));
        assertTrue(Files.isRegularFile(extensionDir.resolve("content-script.js")));
        assertTrue(Files.isRegularFile(extensionDir.resolve("trigger.html")));
        assertTrue(Files.isRegularFile(extensionDir.resolve("trigger.js")));
        assertTrue(Files.isRegularFile(extensionDir.resolve("sidepanel.html")));
        assertTrue(Files.isRegularFile(extensionDir.resolve("sidepanel.js")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> chromeOptions(ChromeOptions options) {
        return (Map<String, Object>) options.asMap().get("goog:chromeOptions");
    }

    private static boolean hasExtensionArgument(List<String> args, String prefix) {
        return args.stream().anyMatch(arg -> arg.startsWith(prefix)
                && arg.replace('\\', '/').contains("extensions/aut-logs-sidepanel"));
    }
}
