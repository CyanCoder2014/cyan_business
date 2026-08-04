export const PLATFORM_AUTH_CLIENT_ID = "cyan-panel";

const ACCESS_TOKEN_STORAGE_KEY = "cyan.panel.authToken";
const REFRESH_TOKEN_STORAGE_KEY = "cyan.panel.refreshToken";
const EXPIRES_AT_STORAGE_KEY = "cyan.panel.authExpiresAt";
const SESSION_ID_STORAGE_KEY = "cyan.panel.sessionId";
const USERNAME_STORAGE_KEY = "cyan.panel.username";
const REFRESH_LEEWAY_MS = 60_000;

type TokenResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  sessionId: string;
};

type CaptchaChallengeResponse = {
  challengeId: string;
  prompt: string;
  expiresAtEpochSecond: number;
};

type UserSummary = {
  username: string;
  email?: string;
  phoneNumber?: string;
  mfaEnabled: boolean;
  roles: string[];
  active: boolean;
};

export type LoginInput = {
  username: string;
  password: string;
  captchaChallengeId: string;
  captchaAnswer: string;
  otpCode?: string;
};

export type RegisterInput = {
  email: string;
  password: string;
  phoneNumber?: string;
};

export class AuthenticationRequiredError extends Error {
  constructor(message = "Authentication is required") {
    super(message);
    this.name = "AuthenticationRequiredError";
  }
}

let refreshPromise: Promise<string> | null = null;

function platformBaseUrl() {
  return process.env.NEXT_PUBLIC_PLATFORM_API_BASE_URL?.replace(/\/$/, "") ?? "http://localhost:18001";
}

function authBaseUrl(path: string) {
  if (path.startsWith("/api/sso/")) {
    return "";
  }
  return platformBaseUrl();
}

function storage() {
  return typeof window === "undefined" ? null : window.localStorage;
}

function now() {
  return Date.now();
}

function normalizeUsername(value: string) {
  const trimmed = value.trim();
  return trimmed.includes("@") ? trimmed.toLowerCase() : trimmed;
}

function currentReturnTo() {
  if (typeof window === "undefined") {
    return "/";
  }
  return `${window.location.pathname}${window.location.search}${window.location.hash}`;
}

function shouldRefreshAccessToken() {
  const stored = storage();
  if (!stored) {
    return false;
  }
  const expiresAt = Number(stored.getItem(EXPIRES_AT_STORAGE_KEY) ?? "0");
  return expiresAt > 0 && now() >= expiresAt - REFRESH_LEEWAY_MS;
}

function getRefreshToken() {
  return storage()?.getItem(REFRESH_TOKEN_STORAGE_KEY) ?? "";
}

function setTokenResponse(response: TokenResponse, username?: string) {
  const stored = storage();
  if (!stored) {
    return;
  }
  stored.setItem(ACCESS_TOKEN_STORAGE_KEY, response.accessToken);
  stored.setItem(REFRESH_TOKEN_STORAGE_KEY, response.refreshToken);
  stored.setItem(EXPIRES_AT_STORAGE_KEY, String(now() + response.expiresIn * 1000));
  stored.setItem(SESSION_ID_STORAGE_KEY, response.sessionId);
  if (username) {
    stored.setItem(USERNAME_STORAGE_KEY, normalizeUsername(username));
  }
}

async function authRequestJson<T>(path: string, init: RequestInit): Promise<T> {
  const response = await fetch(`${authBaseUrl(path)}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init.headers ?? {})
    },
    cache: "no-store"
  });

  if (!response.ok) {
    const body = await response.text().catch(() => "");
    throw new Error(body || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<T>;
}

async function usableAccessToken() {
  const token = getPlatformAuthToken();
  if (!token) {
    return "";
  }
  if (shouldRefreshAccessToken() && getRefreshToken()) {
    return refreshPlatformAuthToken();
  }
  return token;
}

function withAuthorization(init: RequestInit, token: string): RequestInit {
  const headers = new Headers(init.headers);
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  return {
    ...init,
    headers
  };
}

export function getPlatformAuthToken() {
  return storage()?.getItem(ACCESS_TOKEN_STORAGE_KEY) ?? "";
}

export function getPlatformUsername() {
  return storage()?.getItem(USERNAME_STORAGE_KEY) ?? "";
}

export function getPlatformSessionId() {
  return storage()?.getItem(SESSION_ID_STORAGE_KEY) ?? "";
}

export function setPlatformAuthToken(token: string) {
  const stored = storage();
  if (!stored) {
    return;
  }
  if (token.trim()) {
    stored.setItem(ACCESS_TOKEN_STORAGE_KEY, token.trim());
    stored.removeItem(EXPIRES_AT_STORAGE_KEY);
  } else {
    clearPlatformAuthSession();
  }
}

export function clearPlatformAuthSession() {
  const stored = storage();
  if (!stored) {
    return;
  }
  stored.removeItem(ACCESS_TOKEN_STORAGE_KEY);
  stored.removeItem(REFRESH_TOKEN_STORAGE_KEY);
  stored.removeItem(EXPIRES_AT_STORAGE_KEY);
  stored.removeItem(SESSION_ID_STORAGE_KEY);
  stored.removeItem(USERNAME_STORAGE_KEY);
}

export function platformAuthHeaders() {
  const token = getPlatformAuthToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export function redirectToAuth(returnTo = currentReturnTo()) {
  if (typeof window === "undefined" || window.location.pathname === "/auth") {
    return;
  }
  const params = new URLSearchParams({ returnTo });
  window.location.assign(`/auth?${params.toString()}`);
}

export async function logoutPlatformSession() {
  const sessionId = getPlatformSessionId();
  try {
    if (sessionId) {
      await authRequestJson("/api/sso/auth/logout", {
        method: "POST",
        body: JSON.stringify({ sessionId })
      });
    }
  } finally {
    clearPlatformAuthSession();
  }
}

export async function platformFetch(input: RequestInfo | URL, init: RequestInit = {}) {
  const token = await usableAccessToken().catch(() => "");
  let response = await fetch(input, withAuthorization(init, token));

  if (response.status !== 401) {
    return response;
  }

  if (getRefreshToken()) {
    try {
      const refreshedToken = await refreshPlatformAuthToken();
      response = await fetch(input, withAuthorization(init, refreshedToken));
      if (response.status !== 401) {
        return response;
      }
    } catch {
      // Fall through to session clearing and auth redirect.
    }
  }

  clearPlatformAuthSession();
  redirectToAuth();
  throw new AuthenticationRequiredError();
}

export async function createCaptchaChallenge() {
  const search = new URLSearchParams({ clientId: PLATFORM_AUTH_CLIENT_ID });
  return authRequestJson<CaptchaChallengeResponse>(`/api/sso/captcha/challenges?${search.toString()}`, {
    method: "POST",
    body: JSON.stringify({})
  });
}

export async function loginWithPassword(input: LoginInput) {
  const username = normalizeUsername(input.username);
  const response = await authRequestJson<TokenResponse>("/api/sso/auth/login", {
    method: "POST",
    body: JSON.stringify({
      clientId: PLATFORM_AUTH_CLIENT_ID,
      username,
      password: input.password,
      captchaChallengeId: input.captchaChallengeId,
      captchaAnswer: input.captchaAnswer,
      otpCode: input.otpCode,
      deviceId: "panel-web"
    })
  });
  setTokenResponse(response, username);
  return response;
}

export async function registerPanelUser(input: RegisterInput) {
  const email = input.email.trim().toLowerCase();
  return authRequestJson<UserSummary>("/api/sso/users/register", {
    method: "POST",
    body: JSON.stringify({
      username: email,
      password: input.password,
      email,
      phoneNumber: input.phoneNumber?.trim() || null,
      mfaEnabled: false,
      roles: ["user"]
    })
  });
}

export async function refreshPlatformAuthToken() {
  if (refreshPromise) {
    return refreshPromise;
  }

  refreshPromise = (async () => {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      clearPlatformAuthSession();
      throw new AuthenticationRequiredError("Refresh token is missing");
    }

    try {
      const response = await authRequestJson<TokenResponse>("/api/sso/auth/refresh", {
        method: "POST",
        body: JSON.stringify({
          clientId: PLATFORM_AUTH_CLIENT_ID,
          refreshToken
        })
      });
      setTokenResponse(response);
      return response.accessToken;
    } catch (error) {
      clearPlatformAuthSession();
      throw error;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}
