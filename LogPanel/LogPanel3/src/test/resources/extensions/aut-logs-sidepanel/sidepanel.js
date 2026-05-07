const logsEl = document.getElementById("logs");
const statusEl = document.getElementById("status");
const clearButton = document.getElementById("clear");

const DEFAULT_SERVER_ORIGINS = [
  "http://127.0.0.1:17654",
  "http://localhost:17654",
  "http://host.docker.internal:17654",
];

let rendered = 0;
let activeOrigin = null;
let serverOriginsPromise = loadServerOrigins();

function setStatus(text) {
  statusEl.textContent = text;
}

async function loadServerOrigins() {
  const configured = await chrome.storage.local.get("serverOrigin");
  return unique([configured.serverOrigin, ...DEFAULT_SERVER_ORIGINS].filter(Boolean));
}

function unique(values) {
  return [...new Set(values)];
}

function ensureLogContainer() {
  const emptyState = logsEl.querySelector(".empty");
  if (emptyState) {
    emptyState.remove();
  }
}

function render(entry) {
  ensureLogContainer();

  const line = document.createElement("div");
  line.className = "line";

  const meta = document.createElement("div");
  meta.className = "meta";

  const level = document.createElement("span");
  level.className = "level";
  level.textContent = entry.level;

  const time = document.createElement("span");
  time.textContent = new Date(entry.ts).toLocaleTimeString();

  const message = document.createElement("div");
  message.className = "message";
  message.textContent = entry.message;

  meta.append(level, time);
  line.append(meta, message);
  logsEl.appendChild(line);

  rendered += 1;
  setStatus(`Rendered ${rendered} log entr${rendered === 1 ? "y" : "ies"}`);
  window.scrollTo(0, document.body.scrollHeight);
}

function reset() {
  rendered = 0;
  logsEl.innerHTML = '<div class="empty">Ожидаю новые логи...</div>';
  setStatus("Cleared");
}

async function request(path, options = {}) {
  const origins = await serverOriginsPromise;
  const orderedOrigins = activeOrigin
    ? [activeOrigin, ...origins.filter((origin) => origin !== activeOrigin)]
    : origins;

  let lastError = null;
  for (const origin of orderedOrigins) {
    try {
      const response = await fetch(`${origin}${path}`, {
        cache: "no-store",
        ...options,
      });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      activeOrigin = origin;
      return response;
    } catch (error) {
      lastError = error;
    }
  }
  throw lastError ?? new Error("No log server origin is available");
}

async function refresh() {
  const response = await request("/logs");
  const payload = await response.json();
  const logs = payload.logs ?? [];
  if (!logs.length) {
    if (rendered > 0) {
      reset();
    } else {
      setStatus("No logs yet");
    }
    return;
  }

  if (logs.length < rendered) {
    reset();
  }
  for (const entry of logs.slice(rendered)) {
    render(entry);
  }
}

clearButton.addEventListener("click", async () => {
  await request("/clear", { method: "POST" });
  reset();
});

chrome.storage.onChanged.addListener((changes, areaName) => {
  if (areaName !== "local" || !changes.serverOrigin) {
    return;
  }
  activeOrigin = null;
  serverOriginsPromise = loadServerOrigins();
});

refresh().catch((error) => {
  console.error(error);
  setStatus("Waiting for local log server...");
});

window.setInterval(() => {
  refresh().catch((error) => {
    console.error(error);
    setStatus("Waiting for local log server...");
  });
}, 500);
