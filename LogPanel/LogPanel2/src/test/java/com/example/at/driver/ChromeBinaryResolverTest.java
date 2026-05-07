package com.example.at.driver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChromeBinaryResolverTest {

    @TempDir
    Path tempDir;

    private final String originalChromeBinaryPath = System.getProperty("chrome.binary.path");

    @AfterEach
    void restoreChromeBinaryPath() {
        if (originalChromeBinaryPath == null) {
            System.clearProperty("chrome.binary.path");
        } else {
            System.setProperty("chrome.binary.path", originalChromeBinaryPath);
        }
    }

    @Test
    void usesConfiguredChromeBinaryWhenProvided() throws Exception {
        Path binary = tempDir.resolve("Google Chrome for Testing");
        Files.writeString(binary, "#!/bin/sh\n");
        binary.toFile().setExecutable(true);
        System.setProperty("chrome.binary.path", binary.toString());

        assertEquals(binary.toAbsolutePath().normalize(), ChromeBinaryResolver.resolveBrowserBinary().orElseThrow());
    }

    @Test
    void rejectsConfiguredChromeBinaryWhenItIsNotExecutable() {
        Path missingBinary = tempDir.resolve("missing-chrome");
        System.setProperty("chrome.binary.path", missingBinary.toString());

        assertThrows(IllegalArgumentException.class, ChromeBinaryResolver::resolveBrowserBinary);
    }

    @Test
    void findsWindowsChromeBinariesInKnownCacheLayouts() throws Exception {
        Path chromeForTesting = tempDir.resolve(Path.of(
                "chrome-for-testing",
                "win64-123.0.6312.86",
                "chrome-win64",
                "chrome.exe"
        ));
        Path playwrightChromium = tempDir.resolve(Path.of(
                "ms-playwright",
                "chromium-1179",
                "chrome-win",
                "chrome.exe"
        ));
        writeExecutable(chromeForTesting);
        writeExecutable(playwrightChromium);

        List<Path> found = ChromeBinaryResolver.findBrowserBinaries(tempDir).toList();

        assertTrue(found.contains(chromeForTesting.toAbsolutePath().normalize()));
        assertTrue(found.contains(playwrightChromium.toAbsolutePath().normalize()));
    }

    @Test
    void prefersChromeForTestingAndChromiumOverRegularChrome() {
        Path chromeForTesting = Path.of("C:\\tools\\chrome-for-testing\\win64-123\\chrome-win64\\chrome.exe");
        Path playwrightChromium = Path.of("C:\\Users\\me\\AppData\\Local\\ms-playwright\\chromium-1179\\chrome-win\\chrome.exe");
        Path regularChrome = Path.of("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");

        assertTrue(ChromeBinaryResolver.browserPreference(chromeForTesting)
                > ChromeBinaryResolver.browserPreference(regularChrome));
        assertTrue(ChromeBinaryResolver.browserPreference(playwrightChromium)
                > ChromeBinaryResolver.browserPreference(regularChrome));
    }

    private static void writeExecutable(Path binary) throws IOException {
        Files.createDirectories(binary.getParent());
        Files.writeString(binary, "");
        binary.toFile().setExecutable(true);
    }
}
