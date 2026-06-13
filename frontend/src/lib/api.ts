import axios from "axios";
import { getAccessToken } from "./auth";

/**
 * Axios instance for the backend. Base URL is empty by default (same-origin), so
 * requests hit `/api/*` — proxied to the backend in dev, served via CloudFront in
 * prod. Every request carries the current Cognito access token.
 */
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE ?? "",
});

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
