import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
<<<<<<< HEAD
    port: 3007,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
        ws: true
=======
    port: 3000,
    proxy: {
      "/api": {
        target: "http://localhost:8082",
        changeOrigin: true,
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
      },
    },
  },
});
