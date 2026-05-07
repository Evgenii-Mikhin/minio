package com.example.at.logging;

import com.example.at.config.TestConfig;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Executors;

final class LogPanelServer {

    private static final Object LOCK = new Object();
    private static final Deque<LogEntry> LOGS = new ArrayDeque<>();
    private static HttpServer server;

    private LogPanelServer() {
    }

    static void ensureStarted() {
        synchronized (LOCK) {
            if (server != null) {
                return;
            }

            try {
                HttpServer httpServer = HttpServer.create(
                        new InetSocketAddress("127.0.0.1", TestConfig.logPanelServerPort()),
                        0
                );
                httpServer.createContext("/logs", LogPanelServer::handleLogs);
                httpServer.createContext("/clear", LogPanelServer::handleClear);
                httpServer.setExecutor(Executors.newSingleThreadExecutor(task -> {
                    Thread thread = new Thread(task, "at-log-panel-server");
                    thread.setDaemon(true);
                    return thread;
                }));
                httpServer.start();
                server = httpServer;
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to start AT Log Panel server on 127.0.0.1:" + TestConfig.logPanelServerPort(),
                        e
                );
            }
        }
    }

    static void clear() {
        synchronized (LOCK) {
            LOGS.clear();
        }
    }

    static void capture(String level, String message) {
        synchronized (LOCK) {
            if (LOGS.size() >= TestConfig.logPanelBufferSize()) {
                LOGS.removeFirst();
            }
            LOGS.addLast(new LogEntry(System.currentTimeMillis(), level, message));
        }
    }

    private static void handleLogs(HttpExchange exchange) {
        if (handlePreflight(exchange)) {
            return;
        }
        if (!"GET".equals(exchange.getRequestMethod())) {
            writeJson(exchange, 405, "{\"error\":\"method_not_allowed\"}");
            return;
        }

        List<LogEntry> snapshot;
        synchronized (LOCK) {
            snapshot = new ArrayList<>(LOGS);
        }
        writeJson(exchange, 200, logsJson(snapshot));
    }

    private static void handleClear(HttpExchange exchange) {
        if (handlePreflight(exchange)) {
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod()) && !"GET".equals(exchange.getRequestMethod())) {
            writeJson(exchange, 405, "{\"error\":\"method_not_allowed\"}");
            return;
        }

        clear();
        writeJson(exchange, 200, "{\"ok\":true}");
    }

    private static boolean handlePreflight(HttpExchange exchange) {
        if (!"OPTIONS".equals(exchange.getRequestMethod())) {
            return false;
        }
        addCorsHeaders(exchange.getResponseHeaders());
        try {
            exchange.sendResponseHeaders(204, -1);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            exchange.close();
        }
        return true;
    }

    private static void writeJson(HttpExchange exchange, int status, String body) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        addCorsHeaders(exchange.getResponseHeaders());
        try {
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            exchange.close();
        }
    }

    private static void addCorsHeaders(Headers headers) {
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        headers.set("Content-Type", "application/json; charset=utf-8");
    }

    private static String logsJson(List<LogEntry> logs) {
        StringBuilder json = new StringBuilder("{\"logs\":[");
        for (int i = 0; i < logs.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            LogEntry entry = logs.get(i);
            json.append("{\"ts\":")
                    .append(entry.ts())
                    .append(",\"level\":\"")
                    .append(escape(entry.level()))
                    .append("\",\"message\":\"")
                    .append(escape(entry.message()))
                    .append("\"}");
        }
        return json.append("]}").toString();
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private record LogEntry(long ts, String level, String message) {
    }
}
