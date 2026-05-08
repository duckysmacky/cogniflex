import { api } from '@/api/api';
import { ApiProxyKey } from '@/api/proxy/types';
import type { AbstractFunction } from '@/types';

export const API_PROXY_METHODS_MAP = {
  [ApiProxyKey.ANALYZE_TEXT]: api.analyze.analyzeText,
  [ApiProxyKey.ANALYZE_MEDIA]: api.analyze.analyzeMedia,
} satisfies Record<ApiProxyKey, AbstractFunction>;
