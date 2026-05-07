# AT Framework

Простой фреймворк UI-автотестов на Selenium 4 + JUnit 5.

## Стек
- Java 21 (source/target), запускается на JDK 21+
- Maven 3.9+
- Selenium 4.27 (Selenium Manager сам подтягивает драйверы — WebDriverManager не нужен)
- JUnit Jupiter 5.11
- SLF4J + Logback

## Структура

```
src/test/
├── java/com/example/at/
│   ├── config/TestConfig.java        # чтение настроек (sys props → env → properties)
│   ├── driver/DriverFactory.java     # создание WebDriver (Chrome/Firefox)
│   ├── logging/                      # localhost log sink + активация Chrome Side Panel
│   ├── base/BaseTest.java            # @BeforeEach/@AfterEach, lifecycle драйвера
│   ├── pages/                        # Page Objects
│   │   ├── BasePage.java
│   │   ├── DuckDuckGoHomePage.java
│   │   └── DuckDuckGoSearchResultsPage.java
│   └── tests/
│       └── DuckDuckGoSearchTest.java # пример теста
└── resources/
    ├── config.properties
    ├── extensions/aut-logs-sidepanel/ # Chrome extension с side_panel UI
    └── logback-test.xml
```

Дополнительно для Docker Grid:

- `docker-compose.selenium-grid.yml`: Selenium Hub, Chrome node с VNC/noVNC и video sidecar.
- `docker/selenium-node-chrome/Dockerfile`: тонкая надстройка над `selenium/node-chrome` с `xdotool` для открытия Side Panel внутри X/VNC-сессии контейнера.
- `target/selenium-videos/`: mp4-записи Grid-сессий.

## Запуск

```bash
# все тесты: Chrome GUI, mobile emulation, live-log panel рядом с AUT
mvn test

# headless
mvn test -Dheadless=true -Dlog.panel.enabled=false

# Firefox
mvn test -Dbrowser=firefox -Dlog.panel.enabled=false -Dchrome.mobile.enabled=false

# конкретный тест
mvn test -Dtest=DuckDuckGoSearchTest

# временно отключить панель или mobile emulation
mvn test -Dlog.panel.enabled=false
mvn test -Dchrome.mobile.enabled=false
```

## Selenium Grid в Docker

Поднять локальный Grid с Chrome, noVNC и записью видео:

```bash
docker compose -f docker-compose.selenium-grid.yml up -d --build
```

Запустить тесты через Grid:

```bash
mvn test -Pgrid

# или только демонстрационный UI-тест
mvn test -Pgrid -Dtest=DuckDuckGoSearchTest
```

Открыть живую VNC-сессию в браузере:

```text
http://localhost:7900
```

Пароль noVNC по умолчанию: `secret`.

Видео пишется в:

```text
target/selenium-videos/
```

Остановить Grid:

```bash
docker compose -f docker-compose.selenium-grid.yml down
```

Grid-профиль Maven делает несколько важных подмен:

- `selenium.remote.url=http://localhost:4444` — тесты создают `RemoteWebDriver`;
- `chrome.extension.path=/opt/at/extensions/aut-logs-sidepanel` — путь к extension внутри Chrome node;
- `log.panel.server.bind.address=0.0.0.0` — Java log server слушает не только localhost;
- `log.panel.client.origin=http://host.docker.internal:17654` — extension внутри контейнера ходит к log server на хосте;
- `log.panel.open.command=...xdotool...` — Side Panel открывается browser-level shortcut внутри контейнера;
- `selenium.record.video=true` и `selenium.session.name=AT Log Panel Grid Demo` — video sidecar пишет именованный mp4.

На этом ПК Grid-сценарий проверен командой `mvn test -Pgrid`: прошли 11 тестов. Видеозапись появилась в `target/selenium-videos/`, а извлеченный кадр подтвердил, что в записи одновременно видны mobile AUT и `AT Log Panel` справа.

## Предусловия

Для запуска тестов нужны:

- JDK 21+;
- Maven 3.9+;
- доступ в интернет к `https://duckduckgo.com/`;
- доступный Selenium Manager или уже закэшированный ChromeDriver;
- `src/test/resources/config.properties` в test resources.

Для запуска `DuckDuckGoSearchTest` именно в мобильном режиме вместе с Chrome Side Panel должны выполняться дополнительные условия:

- `browser=chrome`;
- `headless=false`;
- `log.panel.enabled=true`;
- `chrome.mobile.enabled=true`;
- доступен Chrome for Testing, Chromium или Chrome с поддержкой Side Panel API; расширение требует Chrome `116+`;
- на Windows браузер ищется в типичных местах установки Chrome, Chrome for Testing, Chromium и Playwright Chromium (`%LOCALAPPDATA%\ms-playwright`);
- если браузер не найден автоматически, задайте `-Dchrome.binary.path=C:\path\to\chrome.exe`;
- порт `127.0.0.1:17654` свободен, потому что панель читает live-log с `http://127.0.0.1:17654/logs`;
- окно Chrome достаточно широкое для mobile viewport и боковой панели; по умолчанию используется `browser.window.width=1400`;
- запуск идет в активной GUI-сессии, потому что Chrome Side Panel является частью UI браузера и визуально не работает в headless;
- на Windows открытие Side Panel использует нативный shortcut `Alt+Shift+L` через `java.awt.Robot`, поэтому заблокированный рабочий стол или фоновая non-interactive сессия могут помешать визуальному открытию панели.

Команды ниже отключают панель или делают ее невозможной:

```bash
mvn test -Dheadless=true
mvn test -Dbrowser=firefox
mvn test -Dlog.panel.enabled=false
```

Важно: Side Panel не является частью DOM страницы DuckDuckGo. Это штатная боковая область Chrome, открытая расширением в том же окне, поэтому Selenium-скриншот страницы не захватывает ее полностью. Для проверки расположения панели рядом с AUT используйте системный или window-level скриншот окна Chrome.

На этом ПК проект проверен командой `mvn test`: прошли 10 тестов, включая `DuckDuckGoSearchTest` в headed Chromium с mobile emulation и видимой `AT Log Panel` справа от AUT.

## Конфигурация

Приоритет: system property (`-Dkey=value`) → env var (`KEY_NAME`) → `config.properties` → default.

| Ключ | По умолчанию | Назначение |
|---|---|---|
| `browser` | `chrome` | `chrome` или `firefox` |
| `headless` | `false` | запуск без GUI |
| `base.url` | `https://duckduckgo.com/` | стартовый URL |
| `explicit.wait.seconds` | `10` | таймаут WebDriverWait |
| `page.load.timeout.seconds` | `30` | таймаут загрузки страницы |
| `log.panel.enabled` | `true` | включает live-log панель через Chrome Side Panel |
| `log.panel.server.port` | `17654` | localhost-порт, с которого панель читает live-лог |
| `log.panel.server.bind.address` | `127.0.0.1` | адрес bind для Java log server; для Docker Grid используется `0.0.0.0` |
| `log.panel.client.origin` | `http://127.0.0.1:17654` | origin, с которого extension читает `/logs`; для Docker Grid используется `http://host.docker.internal:17654` |
| `log.panel.open.command` | пусто | внешняя команда для открытия Side Panel, например `docker exec ... xdotool ...` |
| `log.panel.open.command.timeout.seconds` | `5` | таймаут внешней команды открытия панели |
| `chrome.binary.path` | пусто | путь к Chrome/Chrome for Testing/Chromium; если пусто, ищется в локальном cache и стандартных путях ОС |
| `chrome.extension.path` | пусто | путь к unpacked extension; локально берется из test resources, в Grid это node-local путь |
| `selenium.remote.url` | пусто | если задан, используется `RemoteWebDriver` вместо локального драйвера |
| `selenium.session.name` | `AT Framework test` | имя Selenium-сессии, также используется video sidecar для имени файла |
| `selenium.record.video` | `false` | capability `se:recordVideo` для Selenium video sidecar |
| `chrome.mobile.enabled` | `true` | включает mobile emulation через `deviceMetrics` / `clientHints` |
| `browser.window.x` | `40` | X-позиция окна AUT |
| `browser.window.y` | `80` | Y-позиция окна AUT |
| `browser.window.width` | `1400` | ширина окна браузера: mobile viewport + Chrome Side Panel |
| `browser.window.height` | `1100` | высота окна браузера |

## Live-Log Панель

Реализация сделана как настоящее расширение Chrome Side Panel в том же окне, где открыт AUT:

- Java поднимает локальный endpoint `http://127.0.0.1:17654/logs`;
- `LogPanelAppender` пишет тестовые логи в `BrowserLogSink`;
- Selenium запускает Chrome for Testing/Chromium и грузит unpacked-расширение из `src/test/resources/extensions/aut-logs-sidepanel/`;
- `content-script.js` добавляет на страницу почти невидимый DOM-trigger, который может запросить открытие панели через service worker;
- `content-script.js` также принимает runtime-config `AT_LOG_PANEL_CONFIG` и передает в extension origin log server, чтобы Grid-контейнер мог читать логи с хоста;
- `LogPanelController` дополнительно использует нативный browser shortcut (`Alt+Shift+L` на Windows/Linux, `Command+Shift+Y` на macOS), потому что Selenium Actions не всегда считаются browser-level user gesture для `chrome.sidePanel.open()`;
- extension page вызывает `chrome.sidePanel.open()`, поэтому лог открывается в боковой панели Chrome, а не отдельной вкладкой или окном;
- `sidepanel.html` / `sidepanel.js` читают `/logs` каждые 500 мс и показывают новые записи по мере выполнения теста.

Что важно:

- режим работает только в headed Chrome;
- для Firefox и headless отключайте `log.panel.enabled`;
- resolver сначала предпочитает Chrome for Testing/Chromium, включая Playwright Chromium на Windows, и только потом обычный установленный Chrome;
- панель не внедряется в страницу AUT и не перекрывает mobile viewport, она занимает штатную боковую область Chrome;
- размер/позицию окна можно подстроить через `browser.window.*`.

## Проверка Side Panel

Быстрый функциональный прогон:

```bash
mvn test -Dtest=DuckDuckGoSearchTest
```

Для доказательства, что панель именно видима в Chrome, снимайте не Selenium screenshot страницы, а системный/window-level screenshot окна браузера. На корректном снимке справа от mobile viewport должна быть штатная Chrome Side Panel с заголовком `AT Log Panel` и live-записями вида `Demo step ...` / `live log tick ...`.

## Добавление нового теста

1. Создайте Page Object в `pages/`, наследуя `BasePage`.
2. Создайте тест-класс в `tests/`, наследуя `BaseTest` — драйвер и wait уже инициализированы.
3. Используйте `@Test` + `@DisplayName` из JUnit 5.
