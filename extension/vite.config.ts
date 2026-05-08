import { crx } from '@crxjs/vite-plugin';
import babel from '@rolldown/plugin-babel';
import tailwindcss from '@tailwindcss/vite';
import react, { reactCompilerPreset } from '@vitejs/plugin-react';
import { defineConfig } from 'vite';
import zip from 'vite-plugin-zip-pack';
import manifest from './manifest.config';

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] }),
    crx({ manifest }),
    zip({ outDir: 'release', outFileName: 'cogniflex-release.zip' }),
    tailwindcss(),
  ],
  resolve: {
    tsconfigPaths: true,
  },
});
