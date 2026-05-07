async function openSidePanel() {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
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

document
  .getElementById("at-log-panel-sidepanel-trigger")
  .addEventListener("click", () => {
    openSidePanel().catch((error) => console.error(error));
  });
