import axios, { AxiosError } from 'axios';

export const apiInstance = axios.create({
  // TODO: полумать, почему не берется из енв в итоговой сборке, пока оставить фолбек
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://cogniflex.nikdor.xyz',
});

apiInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error instanceof AxiosError) {
      switch (error.status) {
        case 415:
          throw new AxiosError('Файл данного типа не поддерживается');
        case 413:
          throw new AxiosError('Файл слишком большой');
      }
    }

    return Promise.reject(error);
  },
);
