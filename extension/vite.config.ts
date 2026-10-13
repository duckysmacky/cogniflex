import { crx } from '@crxjs/vite-plugin';
import babel from '@rolldown/plugin-babel';
import tailwindcss from '@tailwindcss/vite';
import react, { reactCompilerPreset } from '@vitejs/plugin-react';
import { defineConfig } from 'vite';
import zip from 'vite-plugin-zip-pack';
import manifest from './manifest.config';
import pkg from './package.json' with { type: 'json' };

// https://vite.dev/config/
export default defineConfig({
  envDir: '..',
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] }),
    crx({ manifest }),
    zip({
      outDir: 'release',
      outFileName: `cogniflex-extension-${pkg.version}.zip`,
    }),
    tailwindcss(),
  ],
  resolve: {
    tsconfigPaths: true,
  },
});
