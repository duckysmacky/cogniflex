import axios, { AxiosError } from 'axios';

export const apiInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
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
