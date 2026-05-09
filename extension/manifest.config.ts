import { defineManifest } from '@crxjs/vite-plugin';

export default defineManifest({
  manifest_version: 3,
  name: 'Cogniflex',
  version: '1.0.0',
  description: 'ИИ-детектор текста, видео и изображений в реальном времени.',
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
