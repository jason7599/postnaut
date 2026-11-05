import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig, loadEnv } from "vite";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");

  return {
    plugins: [react(), tailwindcss()],
    server: {
      // The browser only ever talks to Vite. Vite forwards /api to Spring Boot,
      // so the session cookie works without any CORS setup.
      proxy: {
        "/api": env.BACKEND_URL || "http://localhost:8080",
      },
    },
  };
});
