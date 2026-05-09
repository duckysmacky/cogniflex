import { Button, Input } from '@/components/ui';
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
  const {
    control,
    formState: { isSubmitting, isDirty, isValid },
    reset,
    resetField,
    handleSubmit,
  } = useSettingsForm();
  const setMinTextLength = useSettingsStore((state) => state.setMinTextLength);

  const onSubmit: SubmitHandler<SettingsFormSchema> = async ({ minTextLength }) => {
    setMinTextLength(minTextLength);
    reset({ minTextLength });
  };

  return (
    <div className="flex flex-col gap-2 px-5 py-4">
      <div className="flex flex-col gap-1">
        <h2 className="text-sm font-bold">Настройки</h2>
        <h3 className="text-2xs text-gray">
          Настройте параметры определения ИИ-контента на странице
        </h3>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-2">
        <Controller
          name="minTextLength"
          control={control}
          render={({ field, fieldState: { error, isDirty } }) => (
            <Input
              {...field}
              type="number"
              placeholder="Например, 5000"
              label="Минимальная длина текста (симв.)"
              helperText={`Введите значение от ${MIN_TEXT_LENGTH_MIN} символов. По умолчанию - ${MIN_TEXT_LENGTH_DEFAULT} символов`}
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
          Сохранить
        </Button>
      </form>
    </div>
  );
};
