package com.example.at.driver;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

final class ChromeExtensionResolver {

    private static final String MANIFEST_RESOURCE = "extensions/aut-logs-sidepanel/manifest.json";

    private ChromeExtensionResolver() {
    }

    static Path resolveUnpackedExtensionDir() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL manifestUrl = classLoader.getResource(MANIFEST_RESOURCE);
        if (manifestUrl == null) {
            throw new IllegalStateException("Bundled AT Log Panel extension was not found: " + MANIFEST_RESOURCE);
        }

        Path manifestPath = toPath(manifestUrl);
        Path extensionDir = manifestPath.getParent().toAbsolutePath().normalize();
        if (!Files.isDirectory(extensionDir)) {
            throw new IllegalStateException("AT Log Panel extension path is not a directory: " + extensionDir);
        }
        return extensionDir;
    }

    private static Path toPath(URL url) {
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new IllegalStateException("AT Log Panel extension must be available as local files: " + url, e);
        }
    }
}
