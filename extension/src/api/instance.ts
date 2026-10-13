import { getT } from '@/i18n';
import axios, { AxiosError } from 'axios';

export const apiInstance = axios.create({
  // TODO: полумать, почему не берется из енв в итоговой сборке, пока оставить фолбек
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://cogniflex.nikdor.xyz',
});

apiInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error instanceof AxiosError) {
      const t = getT();
      const message = (error.status && t.errors[error.status]) || t.analyze.failed;

      throw new AxiosError(message, error.code, error.config, error.request, error.response);
    }

    return Promise.reject(error);
  },
);
