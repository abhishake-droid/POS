const AUTH_TOKEN_KEY = 'authToken';
const USER_ID_KEY = 'userId';
const LAST_CHECK_TIME_KEY = 'lastCheckTime';
const AUTH_CHECK_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds

export const authStorage = {
  getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return sessionStorage.getItem(AUTH_TOKEN_KEY);
  },

  setToken(token: string): void {
    if (typeof window === 'undefined') return;
    sessionStorage.setItem(AUTH_TOKEN_KEY, token);
  },

  removeToken(): void {
    if (typeof window === 'undefined') return;
    sessionStorage.removeItem(AUTH_TOKEN_KEY);
  },

  getUserId(): string | null {
    if (typeof window === 'undefined') return null;
    return sessionStorage.getItem(USER_ID_KEY);
  },

  setUserId(userId: string): void {
    if (typeof window === 'undefined') return;
    sessionStorage.setItem(USER_ID_KEY, userId);
  },

  removeUserId(): void {
    if (typeof window === 'undefined') return;
    sessionStorage.removeItem(USER_ID_KEY);
  },

  getLastCheckTime(): number | null {
    if (typeof window === 'undefined') return null;
    const time = sessionStorage.getItem(LAST_CHECK_TIME_KEY);
    return time ? parseInt(time, 10) : null;
  },

  setLastCheckTime(): void {
    if (typeof window === 'undefined') return;
    sessionStorage.setItem(LAST_CHECK_TIME_KEY, Date.now().toString());
  },

  shouldCheckAuth(): boolean {
    const lastCheck = this.getLastCheckTime();
    if (!lastCheck) return true;
    const timeSinceLastCheck = Date.now() - lastCheck;
    return timeSinceLastCheck >= AUTH_CHECK_INTERVAL;
  },

  clearAll(): void {
    if (typeof window === 'undefined') return;
    sessionStorage.removeItem(AUTH_TOKEN_KEY);
    sessionStorage.removeItem(USER_ID_KEY);
    sessionStorage.removeItem(LAST_CHECK_TIME_KEY);
  },
};