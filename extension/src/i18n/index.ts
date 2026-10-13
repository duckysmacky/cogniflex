import { useSettingsStore } from '@/stores';
import { en, type Dictionary } from './en';
import { ru } from './ru';

export type { Dictionary };

export type Language = 'en' | 'ru';
export type LanguageSetting = Language | 'auto';

export const LANGUAGE_NAMES: Record<Language, string> = {
  en: 'English',
  ru: 'Русский',
};

const DICTIONARIES: Record<Language, Dictionary> = { en, ru };

export const resolveLanguage = (setting: LanguageSetting): Language => {
  if (setting !== 'auto') return setting;
  return navigator.language.toLowerCase().startsWith('ru') ? 'ru' : 'en';
};

export const useLanguage = () => resolveLanguage(useSettingsStore((state) => state.language));

export const useT = () => DICTIONARIES[useLanguage()];
