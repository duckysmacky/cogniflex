import { Button, Input } from '@/components/ui';
import { LANGUAGE_NAMES, useT, type LanguageSetting } from '@/i18n';
import { useSettingsStore } from '@/stores';
import { IconReload } from '@tabler/icons-react';
import { Controller, type SubmitHandler } from 'react-hook-form';
import {
  MIN_TEXT_LENGTH_DEFAULT,
  MIN_TEXT_LENGTH_MIN,
  useSettingsForm,
  type SettingsFormSchema,
} from './form';

export const SettingsSection = () => {
  const t = useT();
  const {
    control,
    formState: { isSubmitting, isDirty, isValid },
    reset,
    resetField,
    handleSubmit,
  } = useSettingsForm();
  const setMinTextLength = useSettingsStore((state) => state.setMinTextLength);
  const language = useSettingsStore((state) => state.language);
  const setLanguage = useSettingsStore((state) => state.setLanguage);

  const onSubmit: SubmitHandler<SettingsFormSchema> = async ({ minTextLength }) => {
    setMinTextLength(minTextLength);
    reset({ minTextLength });
  };

  return (
    <div className="flex flex-col gap-2 px-5 py-4">
      <div className="flex flex-col gap-1">
        <h2 className="text-sm font-bold">{t.settings.title}</h2>
        <h3 className="text-2xs text-gray">{t.settings.description}</h3>
      </div>

      <div className="flex flex-col gap-1">
        <label className="text-2xs font-medium" htmlFor="language">
          {t.settings.language}
        </label>
        <select
          id="language"
          value={language}
          onChange={(e) => setLanguage(e.target.value as LanguageSetting)}
          className="bg-dark-blue border-gray/30 cursor-pointer rounded-lg border px-3 py-2 text-xs outline-none focus:border-white"
        >
          <option value="auto">{t.settings.languageAuto}</option>
          {Object.entries(LANGUAGE_NAMES).map(([code, name]) => (
            <option key={code} value={code}>
              {name}
            </option>
          ))}
        </select>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-2">
        <Controller
          name="minTextLength"
          control={control}
          render={({ field, fieldState: { error, isDirty } }) => (
            <Input
              {...field}
              type="number"
              placeholder={t.settings.minTextLengthPlaceholder}
              label={t.settings.minTextLengthLabel}
              helperText={t.settings.minTextLengthHelper(
                MIN_TEXT_LENGTH_MIN,
                MIN_TEXT_LENGTH_DEFAULT,
              )}
              endAdornment={
                isDirty
                  ? () => (
                      <IconReload
                        onClick={() => resetField(field.name)}
                        role="button"
                        size={16}
                        className="text-gray cursor-pointer"
                      />
                    )
                  : undefined
              }
              isError={!!error}
            />
          )}
        />

        <Button disabled={!isValid || !isDirty || isSubmitting} type="submit">
          {t.settings.save}
        </Button>
      </form>
    </div>
  );
};
