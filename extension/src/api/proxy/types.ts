import type { AxiosResponse } from 'axios';

export const ApiProxyKey = {
  ANALYZE_TEXT: 'ANALYZE_TEXT',
  ANALYZE_MEDIA: 'ANALYZE_MEDIA',
} as const;

export type ApiProxyKey = (typeof ApiProxyKey)[keyof typeof ApiProxyKey];

export type ApiProxyMethod<TArgs extends any[]> = (...args: TArgs) => Promise<AxiosResponse>;
