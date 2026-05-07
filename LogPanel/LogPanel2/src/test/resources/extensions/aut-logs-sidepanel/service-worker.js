async function configureSidePanel() {
  await chrome.sidePanel.setPanelBehavior({ openPanelOnActionClick: true });
}

async function openSidePanel(tab) {
  if (!tab?.id) {
    return;
  }

  await chrome.sidePanel.setOptions({
    tabId: tab.id,
    path: "sidepanel.html",
    enabled: true,
  });
  await chrome.sidePanel.open({ tabId: tab.id });
}

chrome.runtime.onInstalled.addListener(() => {
  configureSidePanel().catch((error) => console.error(error));
});

chrome.runtime.onStartup.addListener(() => {
  configureSidePanel().catch((error) => console.error(error));
});

chrome.action.onClicked.addListener((tab) => {
  openSidePanel(tab).catch((error) => console.error(error));
});

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message?.type !== "OPEN_AT_LOG_PANEL") {
    return false;
  }

  openSidePanel(sender.tab)
    .then(() => sendResponse({ ok: true }))
    .catch((error) => {
      console.error(error);
      sendResponse({ ok: false, error: String(error) });
    });
  return true;
});
