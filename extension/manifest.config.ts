import { defineManifest } from '@crxjs/vite-plugin';

export default defineManifest({
  manifest_version: 3,
  name: 'Cogniflex',
  version: '1.0.0',
  description: 'ИИ-детектор текста, видео и изображений в реальном времени.',
  action: {
    default_popup: 'index.html',
  },
  permissions: ['scripting', 'activeTab'],
  host_permissions: ['<all_urls>'],
  content_scripts: [
    {
      matches: ['<all_urls>'],
      js: ['./content.js'],
      run_at: 'document_idle',
    },
  ],
});
