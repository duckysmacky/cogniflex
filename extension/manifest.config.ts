import { defineManifest } from '@crxjs/vite-plugin';

const extensionVersion = process.env.EXTENSION_VERSION ?? '0.0.0';

export default defineManifest({
  manifest_version: 3,
  name: 'Cogniflex',
  version: extensionVersion,
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
