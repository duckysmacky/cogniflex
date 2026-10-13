import { AnalyzeVerdict } from '@/entities/analyze';
import type { Dictionary } from './en';

const CHAR_FORMS: Partial<Record<Intl.LDMLPluralRule, string>> = {
  one: 'символ',
  few: 'символа',
};
const pluralRules = new Intl.PluralRules('ru');

export const ru: Dictionary = {
  header: {
    subtitle: 'ИИ-детектор',
    active: 'Активно',
    inactive: 'Неактивно',
    tooltip: 'Функция определения ИИ-контента на странице',
  },
  footer: {
    about: 'О расширении',
    reportBug: 'Сообщить об ошибке',
  },
  analyze: {
    title: 'Анализ',
    fileInfo: (extension, size) => `Файл ${extension} • ${size}`,
    placeholder: 'Введите текст или переместите файл сюда...',
    charCount: (count) => `${count} ${CHAR_FORMS[pluralRules.select(count)] ?? 'символов'}`,
    run: 'Запустить проверку',
    failed: 'Не удалось выполнить проверку. Попробуйте ещё раз',
  },
  result: {
    analyzing: 'Активно работаем...',
    confidence: 'Уверенность',
    empty: 'Здесь будет результат',
    verdict: {
      [AnalyzeVerdict.HUMAN]: 'Оригинальный контент',
      [AnalyzeVerdict.AI]: 'Сгенерировано ИИ',
    },
  },
  settings: {
    title: 'Настройки',
    description: 'Настройте параметры определения ИИ-контента на странице',
    language: 'Язык',
    languageAuto: 'Как в браузере',
    minTextLengthLabel: 'Минимальная длина текста (симв.)',
    minTextLengthPlaceholder: 'Например, 5000',
    minTextLengthHelper: (min, def) =>
      `Введите значение от ${min} символов. По умолчанию - ${def} символов`,
    save: 'Сохранить',
  },
  errors: {
    413: 'Файл слишком большой',
    415: 'Файл данного типа не поддерживается',
  },
  fileSizeUnits: ['Б', 'КБ', 'МБ', 'ГБ', 'ТБ'],
};
