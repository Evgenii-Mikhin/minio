package com.example.at.logging;

import com.example.at.config.TestConfig;

/**
 * Mirrors framework logs into a local endpoint consumed by Chrome Side Panel.
 */
public final class BrowserLogSink {

    private static final ThreadLocal<Session> SESSIONS = ThreadLocal.withInitial(Session::new);

    private BrowserLogSink() {
    }

    public static void startSession() {
        if (TestConfig.logPanelEnabled()) {
            LogPanelServer.ensureStarted();
            LogPanelServer.clear();
        }
        SESSIONS.set(new Session());
    }

    public static void markRemoteUnavailable() {
        // Navigating the AUT no longer invalidates log delivery.
    }

    public static boolean isRemoteReady() {
        return TestConfig.logPanelEnabled() && SESSIONS.get().panelOpened;
    }

    public static void activatePanel() {
        if (!TestConfig.logPanelEnabled()) {
            return;
        }
        SESSIONS.get().panelOpened = true;
    }

    public static void capture(String level, String message) {
        if (!TestConfig.logPanelEnabled()) {
            return;
        }

        String normalizedMessage = normalize(message);
        if (normalizedMessage.isBlank()) {
            return;
        }

        LogPanelServer.ensureStarted();
        LogPanelServer.capture(level, normalizedMessage);
    }

    public static void reset() {
        SESSIONS.remove();
    }

    private static String normalize(String message) {
        return message == null ? "" : message.replaceAll("\\s+", " ").trim();
    }

    private static final class Session {
        private boolean panelOpened;
    }
}
