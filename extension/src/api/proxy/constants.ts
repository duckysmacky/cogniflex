import { api } from '@/api/api';
import { ApiProxyKey } from '@/api/proxy/types';
import type { AbstractFunction } from '@/types';

export const API_PROXY_METHODS_MAP = {
  [ApiProxyKey.ANALYZE_TEXT]: api.analyze.analyzeText,
  [ApiProxyKey.ANALYZE_MEDIA]: async (url: string) => {
    const response = await fetch(url);
    const blob = await response.blob();
    const filename = url.split('/').pop()?.split('?')[0] || 'media';
    const file = new File([blob], filename, { type: blob.type || 'application/octet-stream' });
    return api.analyze.analyzeMedia(file);
  },
} satisfies Record<ApiProxyKey, AbstractFunction>;
