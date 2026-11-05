import { request } from "./client";

export type Credentials = {
  username: string;
  password: string;
};

export type Registration = Credentials & {
  displayName: string;
};

export const login = (body: Credentials) =>
  request<void>("/auth/login", { method: "POST", body });

export const register = (body: Registration) =>
  request<void>("/auth/register", { method: "POST", body });

export const logout = () => request<void>("/auth/logout", { method: "POST" });
