// Every request to the backend goes through this file.

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, code: string, message: string) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

type RequestOptions = {
  method?: "GET" | "POST";
  body?: unknown;
};

// Spring Security's cookie CSRF setup needs this header. If the backend has CSRF
// turned off, the cookie doesn't exist and the header is skipped.
function readCsrfToken() {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : null;
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const method = options.method ?? "GET";
  const headers: Record<string, string> = {};

  if (options.body !== undefined) headers["Content-Type"] = "application/json";

  const csrfToken = readCsrfToken();
  if (csrfToken && method !== "GET") headers["X-XSRF-TOKEN"] = csrfToken;

  let response: Response;
  try {
    response = await fetch(`/api${path}`, {
      method,
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    });
  } catch {
    throw new ApiError(0, "NETWORK_ERROR", "Could not reach the server.");
  }

  if (response.ok) {
    // 201 and 204 responses from auth endpoints have no body.
    const text = await response.text();
    return (text ? JSON.parse(text) : undefined) as T;
  }

  const error = await readError(response);
  throw new ApiError(response.status, error.code, error.message);
}

async function readError(response: Response) {
  const fallbackCode = response.status === 401 ? "UNAUTHORIZED" : `HTTP_${response.status}`;
  try {
    const data = (await response.json()) as { code?: unknown; message?: unknown };
    return {
      code: typeof data.code === "string" ? data.code : fallbackCode,
      message: typeof data.message === "string" ? data.message : response.statusText,
    };
  } catch {
    return { code: fallbackCode, message: response.statusText };
  }
}

const messages: Record<string, string> = {
  BAD_CREDENTIALS: "That username and password don't match.",
  MAX_LIVE_POST_REACHED: "You've reached the limit for live posts. Wait for one to expire, then try again.",
  NETWORK_ERROR: "Couldn't reach Postnaut. Check your connection and try again.",
  USERNAME_TAKEN: "That username is taken. Try another one.",
  VALIDATION_ERROR: "Check what you entered and try again.",
  VALIDATION_FAILED: "Check what you entered and try again.",
};

export function errorMessage(error: unknown) {
  if (error instanceof ApiError && messages[error.code]) return messages[error.code];
  return "Something went wrong on the server. Try again.";
}
