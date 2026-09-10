import axios, { AxiosError, type AxiosRequestConfig } from "axios";
import type { ApiResult } from "./types";

const TOKEN_KEY = "shophub_token";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  timeout: 12000
});

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.authorization = token;
  }
  return config;
});

export async function request<T>(
  path: string,
  config: AxiosRequestConfig = {}
): Promise<T> {
  try {
    const response = await api.request<ApiResult<T>>({
      url: path,
      ...config
    });
    const result = response.data;
    if (!result.success) {
      throw new Error(result.errorMsg || "Request failed");
    }
    return result.data;
  } catch (error) {
    if (error instanceof AxiosError) {
      if (error.response?.status === 401) {
        clearToken();
        throw new Error("Please sign in again");
      }
      throw new Error(error.response?.data?.errorMsg || error.message);
    }
    throw error;
  }
}

export const http = {
  get: <T>(path: string) => request<T>(path, { method: "GET" }),
  post: <T>(path: string, data?: unknown) =>
    request<T>(path, { method: "POST", data }),
  put: <T>(path: string, data?: unknown) =>
    request<T>(path, { method: "PUT", data }),
  upload: <T>(path: string, data: FormData) =>
    request<T>(path, {
      method: "POST",
      data,
      headers: { "Content-Type": "multipart/form-data" }
    })
};
