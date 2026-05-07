import { settingsFormSchema } from '@/sections/settings/form/schema';
import { useSettingsStore } from '@/stores';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { MIN_TEXT_LENGTH_DEFAULT } from './constants';

export const useSettingsForm = () => {
  const form = useForm({
    resolver: zodResolver(settingsFormSchema),
    defaultValues: {
      minTextLength: MIN_TEXT_LENGTH_DEFAULT,
    },
    mode: 'onChange',
  });

  const minTextLength = useSettingsStore((state) => state.minTextLength);

  useEffect(() => {
    form.reset({ minTextLength });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [form]);

  return form;
};
