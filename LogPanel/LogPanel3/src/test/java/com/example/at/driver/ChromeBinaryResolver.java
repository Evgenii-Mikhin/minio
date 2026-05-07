package com.example.at.driver;

import com.example.at.config.TestConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

final class ChromeBinaryResolver {

    private static final int MAX_SEARCH_DEPTH = 8;
    private static final List<String> BROWSER_BINARY_SUFFIXES = List.of(
            "chrome-win64/chrome.exe",
            "chrome-win/chrome.exe",
            "google/chrome for testing/application/chrome.exe",
            "google/chrome/application/chrome.exe",
            "chromium/application/chrome.exe",
            "chrome-mac-arm64/google chrome for testing.app/contents/macos/google chrome for testing",
            "chrome-mac-x64/google chrome for testing.app/contents/macos/google chrome for testing",
            "google chrome for testing.app/contents/macos/google chrome for testing",
            "chromium.app/contents/macos/chromium",
            "google chrome.app/contents/macos/google chrome",
            "chrome-linux64/chrome",
            "chrome-linux/chrome",
            "/usr/bin/google-chrome",
            "/usr/bin/chromium",
            "/usr/bin/chromium-browser",
            "/snap/bin/chromium"
    );

    private ChromeBinaryResolver() {
    }

    static Optional<Path> resolveBrowserBinary() {
        String configuredBinary = TestConfig.chromeBinaryPath();
        if (!configuredBinary.isBlank()) {
            return Optional.of(validate(Path.of(configuredBinary).toAbsolutePath().normalize()));
        }

        Set<Path> candidates = new LinkedHashSet<>();
        explicitBrowserPaths().stream()
                .map(Path::toAbsolutePath)
                .map(Path::normalize)
                .forEach(candidates::add);
        searchRoots().stream()
                .flatMap(ChromeBinaryResolver::findBrowserBinaries)
                .forEach(candidates::add);

        return candidates.stream()
                .filter(ChromeBinaryResolver::isExecutableFile)
                .max(Comparator.comparingInt(ChromeBinaryResolver::browserPreference)
                        .thenComparing(ChromeBinaryResolver::versionKey));
    }

    static Path requireSidePanelBrowserBinary() {
        return resolveBrowserBinary()
                .orElseThrow(() -> new IllegalStateException(
                        "Chrome Side Panel tests require a Chrome-family browser with extension support. "
                                + "Install Chrome for Testing/Chromium or set "
                                + "-Dchrome.binary.path=C:\\path\\to\\chrome.exe"
                ));
    }

    static Stream<Path> findBrowserBinaries(Path root) {
        if (!Files.isDirectory(root)) {
            return Stream.empty();
        }

        try {
            return Files.find(root, MAX_SEARCH_DEPTH, (path, attrs) ->
                            attrs.isRegularFile() && isBrowserExecutableCandidate(path))
                    .map(Path::toAbsolutePath)
                    .map(Path::normalize);
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    static boolean isBrowserExecutableCandidate(Path path) {
        String normalized = normalize(path);
        return BROWSER_BINARY_SUFFIXES.stream().anyMatch(normalized::endsWith);
    }

    private static Path validate(Path binary) {
        if (!isExecutableFile(binary)) {
            throw new IllegalArgumentException("Configured Chrome binary is not executable: " + binary);
        }
        return binary;
    }

    private static List<Path> explicitBrowserPaths() {
        List<Path> paths = new ArrayList<>();

        add(paths, System.getenv("ProgramFiles"), "Google", "Chrome for Testing", "Application", "chrome.exe");
        add(paths, System.getenv("ProgramFiles"), "Google", "Chrome", "Application", "chrome.exe");
        add(paths, System.getenv("ProgramFiles"), "Chromium", "Application", "chrome.exe");
        add(paths, System.getenv("ProgramFiles(x86)"), "Google", "Chrome", "Application", "chrome.exe");
        add(paths, System.getenv("ProgramFiles(x86)"), "Chromium", "Application", "chrome.exe");
        add(paths, System.getenv("LOCALAPPDATA"), "Google", "Chrome for Testing", "Application", "chrome.exe");
        add(paths, System.getenv("LOCALAPPDATA"), "Google", "Chrome", "Application", "chrome.exe");
        add(paths, System.getenv("LOCALAPPDATA"), "Chromium", "Application", "chrome.exe");

        paths.add(Path.of("/Applications/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing"));
        paths.add(Path.of("/Applications/Chromium.app/Contents/MacOS/Chromium"));
        paths.add(Path.of("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"));
        paths.add(Path.of("/usr/bin/google-chrome"));
        paths.add(Path.of("/usr/bin/chromium"));
        paths.add(Path.of("/usr/bin/chromium-browser"));
        paths.add(Path.of("/snap/bin/chromium"));

        return paths;
    }

    private static List<Path> searchRoots() {
        List<Path> roots = new ArrayList<>();
        String home = System.getProperty("user.home");
        if (home != null && !home.isBlank()) {
            roots.add(Path.of(home, ".cache", "at", "chrome-for-testing"));
            roots.add(Path.of(home, ".cache", "puppeteer", "chrome"));
        }
        add(roots, System.getenv("LOCALAPPDATA"), "ms-playwright");
        return roots;
    }

    private static void add(List<Path> paths, String first, String... more) {
        if (first != null && !first.isBlank()) {
            paths.add(Path.of(first, more));
        }
    }

    private static boolean isExecutableFile(Path binary) {
        return Files.isRegularFile(binary) && Files.isExecutable(binary);
    }

    private static String versionKey(Path binary) {
        String path = normalize(binary);
        return Stream.of("chrome-for-testing/", "chromium-", "win64-", "mac_arm-", "mac-", "linux-")
                .filter(path::contains)
                .findFirst()
                .map(marker -> path.substring(path.indexOf(marker) + marker.length()))
                .orElse(path);
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
    }

    static int browserPreference(Path binary) {
        String path = normalize(binary);
        if (path.contains("chrome-for-testing") || path.contains("chrome for testing")) {
            return 4;
        }
        if (path.contains("chromium") || path.contains("ms-playwright")) {
            return 3;
        }
        if (path.contains("google/chrome") || path.contains("google chrome.app")) {
            return 2;
        }
        return 1;
    }
}
