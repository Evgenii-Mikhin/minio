package com.example.at.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Mirrors regular test logs into BrowserLogSink so they appear in Chrome Side Panel.
 */
public final class LogPanelAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (eventObject == null) {
            return;
        }
        BrowserLogSink.capture(eventObject.getLevel().levelStr, eventObject.getFormattedMessage());
    }
}
