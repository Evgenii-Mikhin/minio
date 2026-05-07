package com.example.at.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestConfigTest {

    private static final String[] DEFAULT_KEYS = {
            "headless",
            "chrome.mobile.enabled",
            "log.panel.enabled"
    };

    private final Map<String, String> originalProperties = new HashMap<>();

    @BeforeEach
    void clearSystemPropertyOverrides() {
        for (String key : DEFAULT_KEYS) {
            originalProperties.put(key, System.getProperty(key));
            System.clearProperty(key);
        }
    }

    @AfterEach
    void restoreSystemPropertyOverrides() {
        for (Map.Entry<String, String> entry : originalProperties.entrySet()) {
            if (entry.getValue() == null) {
                System.clearProperty(entry.getKey());
            } else {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    @Test
    void startsInHeadedChromeWithMobileLogPanelByDefault() {
        assertFalse(TestConfig.headless(), "Log panel needs headed Chrome by default");
        assertTrue(TestConfig.chromeMobileEnabled(), "Autotest should start with Chrome mobile emulation");
        assertTrue(TestConfig.logPanelEnabled(), "Autotest logs should be visible in Chrome Side Panel");
    }
}
