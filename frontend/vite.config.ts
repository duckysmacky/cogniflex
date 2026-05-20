import { defineConfig, loadEnv } from 'vite';
import react, { reactCompilerPreset } from '@vitejs/plugin-react';
import babel from '@rolldown/plugin-babel';

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '..', '');
  const websitePort = env.WEBSITE_PORT ? Number(env.WEBSITE_PORT) : undefined;

  return {
    envDir: '..',
    plugins: [react(), babel({ presets: [reactCompilerPreset()] })],
    server: {
      port: websitePort,
    },
  };
});
