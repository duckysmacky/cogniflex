import { defineManifest } from '@crxjs/vite-plugin';
import pkg from './package.json' with { type: 'json' };

export default defineManifest({
  manifest_version: 3,
  name: 'Cogniflex',
  version: pkg.version,
  description: 'Real-time AI detector for text, images and videos.',
  action: {
    default_popup: 'index.html',
  },
  permissions: ['scripting', 'activeTab', 'storage'],
  host_permissions: ['<all_urls>'],
  background: {
    service_worker: './src/background.js',
    type: 'module',
  },
  content_scripts: [
    {
      matches: ['<all_urls>'],
      js: ['./src/content.js'],
      run_at: 'document_idle',
    },
  ],
});
