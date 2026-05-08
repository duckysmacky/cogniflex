import { apiInstance } from './instance';
import type { AnalyzeMediaResponse, AnalyzeTextResponse } from './types';

export const api = {
  analyze: {
    analyzeText: (text: string) =>
      apiInstance.post<AnalyzeTextResponse>('/api/analyze/text', { text }),
    analyzeMedia: (file: File) => {
      const formData = new FormData();
      formData.append('file', file);
      return apiInstance.post<AnalyzeMediaResponse>('/api/analyze/media', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
    },
  },
};
