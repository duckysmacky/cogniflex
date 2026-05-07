import { MIN_TEXT_LENGTH_DEFAULT } from '@/sections';
import { create } from 'zustand';
import { ChromeLocalStorage } from 'zustand-chrome-storage';
import { createJSONStorage, persist } from 'zustand/middleware';

interface State {
  detectEnabled: boolean;
  minTextLength: number;
  setDetectEnabled: (detectEnabled: boolean) => void;
  setMinTextLength: (minTextLength: number) => void;
}

export const useSettingsStore = create(
  persist<State>(
    (set) => ({
      detectEnabled: false,
      minTextLength: MIN_TEXT_LENGTH_DEFAULT,
      setDetectEnabled: (detectEnabled) => set({ detectEnabled }),
      setMinTextLength: (minTextLength) => set({ minTextLength }),
    }),
    {
      name: 'settings-storage',
      storage: createJSONStorage(() => ChromeLocalStorage),
    },
  ),
);
