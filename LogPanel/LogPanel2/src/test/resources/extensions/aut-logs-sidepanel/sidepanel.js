const logsEl = document.getElementById("logs");
const statusEl = document.getElementById("status");
const clearButton = document.getElementById("clear");

const SERVER_ORIGIN = "http://127.0.0.1:17654";
const LOGS_URL = `${SERVER_ORIGIN}/logs`;
const CLEAR_URL = `${SERVER_ORIGIN}/clear`;

let rendered = 0;

function setStatus(text) {
  statusEl.textContent = text;
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

async function refresh() {
  const response = await fetch(LOGS_URL, { cache: "no-store" });
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
  await fetch(CLEAR_URL, { method: "POST" });
  reset();
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
