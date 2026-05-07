const TRIGGER_ID = "at-log-panel-sidepanel-trigger";
const TRIGGER_FRAME_ID = "at-log-panel-sidepanel-trigger-frame";

function requestSidePanel() {
  chrome.runtime.sendMessage({ type: "OPEN_AT_LOG_PANEL" }).catch((error) => {
    console.error(error);
  });
}

function ensureTrigger() {
  if (document.getElementById(TRIGGER_ID) || document.getElementById(TRIGGER_FRAME_ID)) {
    return;
  }

  const button = document.createElement("button");
  button.id = TRIGGER_ID;
  button.type = "button";
  button.setAttribute("aria-label", "Open AT logs");
  button.addEventListener("click", requestSidePanel);
  button.style.cssText = [
    "position:fixed",
    "top:0",
    "right:0",
    "width:12px",
    "height:12px",
    "opacity:0.01",
    "pointer-events:auto",
    "z-index:2147483647",
    "border:0",
    "padding:0",
    "margin:0",
    "background:transparent",
  ].join(";");

  document.documentElement.appendChild(button);
}

ensureTrigger();
