const UNITS = ['Б', 'КБ', 'МБ', 'ГБ', 'ТБ'] as const;

export const getFileExtension = (file: File) => file.name.split('.').pop() ?? '';

export function formatFileSize(bytes: number, decimals = 2) {
  if (bytes === 0) return '0 Б';

  const i = Math.floor(Math.log2(bytes) / 10);
  const value = bytes / 1024 ** i;

  return `${Number(value.toFixed(decimals))} ${UNITS[i]}`;
}
