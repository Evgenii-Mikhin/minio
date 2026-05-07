package com.example.at.tests;

import com.example.at.base.BaseTest;
import com.example.at.pages.DuckDuckGoHomePage;
import com.example.at.pages.DuckDuckGoSearchResultsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DuckDuckGo search")
class DuckDuckGoSearchTest extends BaseTest {

    private static final Duration MINIMUM_DEMO_DURATION = Duration.ofSeconds(30);
    private static final Duration ACTION_PAUSE = Duration.ofMillis(2_500);

    @Test
    @DisplayName("runs a 30 second mobile search demo with live Side Panel logs")
    void searchReturnsRelevantResults() {
        long startedAt = System.nanoTime();
        String firstQuery = "Selenium WebDriver";
        String secondQuery = "JUnit 5 Selenium";

        logDemoStep(1, "Opening DuckDuckGo home page in mobile emulation");
        DuckDuckGoSearchResultsPage results = new DuckDuckGoHomePage(driver, wait)
                .open()
                .searchFor(firstQuery)
                .waitUntilLoaded();
        logDemoStep(2, "Submitted first query '{}'", firstQuery);
        streamLiveLogs("First result set is visible", ACTION_PAUSE);

        results.scrollBy(520);
        logDemoStep(3, "Scrolled down first result set to y={}", results.scrollY());
        streamLiveLogs("Watching first scroll position", ACTION_PAUSE);

        results.scrollBy(820);
        logDemoStep(4, "Scrolled deeper into first result set to y={}", results.scrollY());
        streamLiveLogs("Watching deeper result list", ACTION_PAUSE);

        results.scrollBy(-420);
        logDemoStep(5, "Scrolled back up within first result set to y={}", results.scrollY());
        streamLiveLogs("Watching upward scroll", ACTION_PAUSE);

        results = results.replaceQuery(secondQuery);
        logDemoStep(6, "Replaced search query with '{}'", secondQuery);
        streamLiveLogs("Second result set is visible", ACTION_PAUSE);

        results.scrollBy(700);
        logDemoStep(7, "Scrolled second result set to y={}", results.scrollY());
        streamLiveLogs("Watching second query results", ACTION_PAUSE);

        driver.navigate().back();
        results.waitUntilLoaded();
        logDemoStep(8, "Navigated back; current query is '{}'", results.searchQuery());
        streamLiveLogs("Back navigation completed", ACTION_PAUSE);

        driver.navigate().forward();
        results.waitUntilLoaded();
        logDemoStep(9, "Navigated forward; current query is '{}'", results.searchQuery());
        streamLiveLogs("Forward navigation completed", ACTION_PAUSE);

        driver.navigate().refresh();
        results.waitUntilLoaded();
        logDemoStep(10, "Refreshed page; title is '{}'", results.pageTitle());
        streamLiveLogs("Refresh completed", ACTION_PAUSE);

        results.scrollToTop();
        logDemoStep(11, "Returned to top of the result page, y={}", results.scrollY());
        streamLiveLogs("Final top-of-page view", ACTION_PAUSE);

        ensureMinimumDemoDuration(startedAt);

        String encodedQuery = URLEncoder.encode(secondQuery, StandardCharsets.UTF_8);
        String actualTitle = results.pageTitle();
        String actualQuery = results.searchQuery();
        String actualUrl = results.currentUrl();
        boolean hasResultsLayout = results.hasResultsLayout();
        Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
        log.info("Current query in search box: {}", actualQuery);

        assertAll(
                () -> assertTrue(
                        actualTitle.toLowerCase().contains(secondQuery.toLowerCase()),
                        () -> "Page title should contain query. Actual: " + actualTitle),
                () -> assertEquals(
                        secondQuery,
                        actualQuery,
                        "Search box should keep the submitted query"),
                () -> assertTrue(
                        hasResultsLayout,
                        "Results layout should be present"),
                () -> assertTrue(
                        actualUrl.contains("q=" + encodedQuery),
                        () -> "Current URL should contain query. Actual: " + actualUrl),
                () -> assertTrue(
                        elapsed.compareTo(MINIMUM_DEMO_DURATION) >= 0,
                        "Demo scenario should run for at least 30 seconds so live logs are visible")
        );
    }

    private void logDemoStep(int step, String message, Object... args) {
        log.info("Demo step {}/11: " + message, prepend(step, args));
    }

    private void streamLiveLogs(String label, Duration duration) {
        long deadline = System.nanoTime() + duration.toNanos();
        int tick = 1;
        while (System.nanoTime() < deadline) {
            log.info("{} - live log tick {}", label, tick++);
            sleep(Duration.ofSeconds(1));
        }
    }

    private void ensureMinimumDemoDuration(long startedAt) {
        while (Duration.ofNanos(System.nanoTime() - startedAt).compareTo(MINIMUM_DEMO_DURATION) < 0) {
            long elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startedAt);
            log.info("Keeping demo alive for Side Panel visibility: {}s / {}s",
                    elapsedSeconds,
                    MINIMUM_DEMO_DURATION.toSeconds());
            sleep(Duration.ofSeconds(1));
        }
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Demo sleep interrupted", e);
        }
    }

    private Object[] prepend(Object first, Object[] rest) {
        Object[] values = new Object[rest.length + 1];
        values[0] = first;
        System.arraycopy(rest, 0, values, 1, rest.length);
        return values;
    }
}
