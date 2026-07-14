export const PORTAL_SESSION_KEY = 'aviva.portal.session';
export const SESSION_EXPIRED_EVENT = 'aviva:portal-session-expired';

function decodeBase64Url(value) {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
  const binary = globalThis.atob(padded);
  const bytes = Uint8Array.from(binary, (character) => character.charCodeAt(0));
  return new TextDecoder().decode(bytes);
}

export function decodeJwtPayload(token) {
  try {
    const parts = token?.split('.');
    if (parts?.length !== 3) return null;
    return JSON.parse(decodeBase64Url(parts[1]));
  } catch {
    return null;
  }
}

export function isTokenExpired(token, nowMs = Date.now()) {
  const payload = decodeJwtPayload(token);
  if (!Number.isFinite(payload?.exp)) return true;
  return payload.exp * 1000 <= nowMs;
}

export function isPatientSession(session, nowMs = Date.now()) {
  return Boolean(
    session?.token
      && session?.role === 'PATIENT'
      && !isTokenExpired(session.token, nowMs),
  );
}

export function createPatientSession(loginResponse) {
  const tokenPayload = decodeJwtPayload(loginResponse?.token);
  const responseRole = loginResponse?.role;
  const tokenRole = tokenPayload?.role;
  const role = tokenRole || responseRole;

  if (role !== 'PATIENT' || (responseRole && responseRole !== tokenRole)) {
    throw new Error('Este portal es exclusivo para pacientes.');
  }
  if (!loginResponse?.token || isTokenExpired(loginResponse.token)) {
    throw new Error('La sesión recibida no es válida. Vuelve a iniciar sesión.');
  }

  return {
    token: loginResponse.token,
    role,
    username: loginResponse.username || tokenPayload?.sub || '',
    firstName: loginResponse.firstName || '',
    lastName: loginResponse.lastName || '',
  };
}

export function readPortalSession() {
  if (typeof window === 'undefined') return null;

  try {
    const serialized = window.localStorage.getItem(PORTAL_SESSION_KEY);
    if (!serialized) return null;
    const session = JSON.parse(serialized);
    if (!isPatientSession(session)) {
      clearPortalSession();
      return null;
    }
    return session;
  } catch {
    clearPortalSession();
    return null;
  }
}

export function writePortalSession(session) {
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(PORTAL_SESSION_KEY, JSON.stringify(session));
  }
}

export function clearPortalSession() {
  if (typeof window !== 'undefined') {
    window.localStorage.removeItem(PORTAL_SESSION_KEY);
  }
}
