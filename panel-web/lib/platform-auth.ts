const STORAGE_KEY = "cyan.panel.authToken";

export function getPlatformAuthToken() {
  if (typeof window === "undefined") {
    return "";
  }
  return window.localStorage.getItem(STORAGE_KEY) ?? "";
}

export function setPlatformAuthToken(token: string) {
  if (typeof window === "undefined") {
    return;
  }
  if (token.trim()) {
    window.localStorage.setItem(STORAGE_KEY, token.trim());
  } else {
    window.localStorage.removeItem(STORAGE_KEY);
  }
}

export function platformAuthHeaders() {
  const token = getPlatformAuthToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}
