# Repository Guidelines

## Project Structure & Module Organization

This is a Java 21 Maven test framework. There is no `src/main` tree; code lives under `src/test/java/com/example/at/`.

- `base/`: shared JUnit lifecycle in `BaseTest`.
- `config/`: runtime configuration lookup from system properties, environment variables, and `config.properties`.
- `driver/`: Selenium WebDriver creation, Chrome binary, extension, and mobile emulation support.
- `logging/`: localhost log panel server, appender, browser log sink, and Chrome Side Panel activation.
- `pages/`: Selenium Page Objects such as `DuckDuckGoHomePage`.
- `tests/`: end-to-end UI tests.
- `src/test/resources/`: `config.properties`, `logback-test.xml`, and the unpacked Chrome Side Panel extension.

## Build, Test, and Development Commands

- `mvn test`: run all JUnit 5 tests with default Chrome, mobile emulation, and live log panel settings.
- `mvn test -Dheadless=true -Dlog.panel.enabled=false`: run tests without a visible browser; disables the side panel.
- `mvn test -Dbrowser=firefox -Dlog.panel.enabled=false -Dchrome.mobile.enabled=false`: run against Firefox.
- `mvn test -Dtest=DuckDuckGoSearchTest`: run one test class.
- `mvn test "-Dtest=ChromeBinaryResolverTest,DriverFactoryTest"`: run a comma-separated test list from PowerShell; quote `-Dtest=...` so commas are not parsed by PowerShell.

Use `-Dkey=value` overrides locally. Precedence is system property, environment variable, `config.properties`, then code defaults.

## Coding Style & Naming Conventions

Use Java 21, UTF-8, and 4-space indentation. Keep package names lowercase. Use `UpperCamelCase` for classes, `lowerCamelCase` for methods and variables, and `UPPER_SNAKE_CASE` for constants. Page Objects should live in `pages/`, expose intent-level actions, and return `this` or the next page object. Test classes should end in `Test`; test methods should be descriptive camelCase, for example `chromeOptionsEnableMobileEmulationAndLoadSidePanelExtensionByDefault`.

No formatter or linter plugin is configured; match existing style and import ordering.

## Testing Guidelines

Tests use JUnit Jupiter 5 and Selenium 4. Put shared browser setup in `BaseTest`; avoid duplicating WebDriver lifecycle code. Unit-style tests for config, driver, and logging helpers can stay beside their package under `src/test/java`. UI tests should extend `BaseTest` and use Page Objects instead of raw selectors in test methods. There is no configured coverage threshold.

Chrome Side Panel is browser UI, not page DOM. A Selenium page screenshot is not sufficient to prove that the panel is visible. When validating visible side panel behavior, use a system or window-level screenshot of the Chrome/Chromium window and confirm the `AT Log Panel` side panel appears beside the mobile AUT viewport.

On Windows, Side Panel opening depends on a headed, interactive GUI session. `LogPanelController` uses a content-script trigger and then a native `java.awt.Robot` shortcut fallback (`Alt+Shift+L`; macOS uses `Command+Shift+Y`) because Selenium Actions may not count as a browser-level user gesture for `chrome.sidePanel.open()`.

## Commit & Pull Request Guidelines

This workspace does not include `.git` history, so no repository-specific commit convention can be inferred. Use short, imperative subjects such as `Add side panel log sink test` and keep unrelated changes separate. Pull requests should include the test command run, configuration overrides, linked issues if applicable, and screenshots or notes for visible Chrome Side Panel behavior.

## Security & Configuration Tips

Do not commit machine-specific browser paths, credentials, or generated browser profiles. Keep local overrides on the command line or in environment variables. The live log panel uses `127.0.0.1:17654` by default; change `log.panel.server.port` if that port is occupied.

`ChromeBinaryResolver` is intentionally cross-platform. Keep Windows support for Chrome for Testing, Chromium, regular Chrome, and Playwright Chromium under `%LOCALAPPDATA%\ms-playwright`; prefer adding tests for new browser search layouts instead of hard-coding a local machine path.
