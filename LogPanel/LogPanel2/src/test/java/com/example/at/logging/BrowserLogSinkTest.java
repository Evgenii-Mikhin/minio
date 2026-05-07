package com.example.at.logging;

import com.example.at.config.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BrowserLogSinkTest {

    @AfterEach
    void tearDown() {
        BrowserLogSink.reset();
    }

    @Test
    void exposesCapturedLogsForChromeSidePanelOverLocalHttp() throws Exception {
        BrowserLogSink.startSession();

        BrowserLogSink.capture("INFO", "Opening home page");

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + TestConfig.logPanelServerPort() + "/logs"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertTrue(response.body().contains("\"level\":\"INFO\""));
        assertTrue(response.body().contains("\"message\":\"Opening home page\""));
    }

    @Test
    void clearsCapturedLogsForChromeSidePanelOverLocalHttp() throws Exception {
        BrowserLogSink.startSession();
        BrowserLogSink.capture("INFO", "Opening home page");

        HttpClient client = HttpClient.newHttpClient();
        client.send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + TestConfig.logPanelServerPort() + "/clear"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + TestConfig.logPanelServerPort() + "/logs"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertTrue(response.body().contains("\"logs\":[]"));
    }
}
