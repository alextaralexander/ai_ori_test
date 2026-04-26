import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './src/tests/generated',
  use: {
    baseURL: process.env.BESTORIGIN_FRONTEND_URL ?? 'http://127.0.0.1:5173',
    locale: 'ru-RU'
  }
});
