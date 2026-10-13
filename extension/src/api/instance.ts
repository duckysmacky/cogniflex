import axios from 'axios';

export const apiInstance = axios.create({
  // TODO: полумать, почему не берется из енв в итоговой сборке, пока оставить фолбек
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://cogniflex.nikdor.xyz',
});
