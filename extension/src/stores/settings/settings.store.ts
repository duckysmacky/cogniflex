import type { LanguageSetting } from '@/i18n';
import { MIN_TEXT_LENGTH_DEFAULT } from '@/sections';
import { create } from 'zustand';
import { ChromeLocalStorage } from 'zustand-chrome-storage';
import { createJSONStorage, persist } from 'zustand/middleware';

interface State {
  detectEnabled: boolean;
  minTextLength: number;
  language: LanguageSetting;
  setDetectEnabled: (detectEnabled: boolean) => void;
  setMinTextLength: (minTextLength: number) => void;
  setLanguage: (language: LanguageSetting) => void;
}

export const useSettingsStore = create(
  persist<State>(
    (set) => ({
      detectEnabled: false,
      minTextLength: MIN_TEXT_LENGTH_DEFAULT,
      language: 'auto',
      setDetectEnabled: (detectEnabled) => set({ detectEnabled }),
      setMinTextLength: (minTextLength) => set({ minTextLength }),
      setLanguage: (language) => set({ language }),
    }),
    {
      name: 'settings-storage',
      storage: createJSONStorage(() => ChromeLocalStorage),
    },
  ),
);
