const AUTH_TOKEN_KEY = 'authToken';
const USER_ID_KEY = 'userId';
const LAST_CHECK_TIME_KEY = 'lastCheckTime';
const AUTH_CHECK_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds

export const authStorage = {
  getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(AUTH_TOKEN_KEY);
  },

  setToken(token: string): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem(AUTH_TOKEN_KEY, token);
  },

  removeToken(): void {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(AUTH_TOKEN_KEY);
  },

  getUserId(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(USER_ID_KEY);
  },

  setUserId(userId: string): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem(USER_ID_KEY, userId);
  },

  removeUserId(): void {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(USER_ID_KEY);
  },

  getLastCheckTime(): number | null {
    if (typeof window === 'undefined') return null;
    const time = localStorage.getItem(LAST_CHECK_TIME_KEY);
    return time ? parseInt(time, 10) : null;
  },

  setLastCheckTime(): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem(LAST_CHECK_TIME_KEY, Date.now().toString());
  },

  shouldCheckAuth(): boolean {
    const lastCheck = this.getLastCheckTime();
    if (!lastCheck) return true;
    const timeSinceLastCheck = Date.now() - lastCheck;
    return timeSinceLastCheck >= AUTH_CHECK_INTERVAL;
  },

  clearAll(): void {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(USER_ID_KEY);
    localStorage.removeItem(LAST_CHECK_TIME_KEY);
  },
};