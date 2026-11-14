import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  base: './',
  server: { allowedHosts: ['.us5.manus.computer'] },
  build: { outDir: 'dist', sourcemap: false },
});
