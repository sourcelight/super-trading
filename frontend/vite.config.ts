import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "node:path";

// Dev server proxies API + WebSocket to the local backend so the SPA can call
// relative paths (/api, /ws) exactly as it will behind CloudFront in production.
export default defineConfig({
  plugins: [react()],
  // sockjs-client references the Node `global`; map it to the browser global.
  define: {
    global: "globalThis",
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: process.env.VITE_API_PROXY ?? "http://localhost:8080",
        changeOrigin: true,
      },
      "/ws": {
        target: process.env.VITE_API_PROXY ?? "http://localhost:8080",
        changeOrigin: true,
        ws: true,
      },
    },
  },
});
