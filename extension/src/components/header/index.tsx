import { Switch } from '@/components/ui';
import { useSettingsStore } from '@/stores';
import { IconSettings } from '@tabler/icons-react';
import { useLocation, useNavigate } from 'react-router-dom';
import { twMerge } from 'tailwind-merge';

export const Header = () => {
  const detectEnabled = useSettingsStore((state) => state.detectEnabled);
  const setDetectEnabled = useSettingsStore((state) => state.setDetectEnabled);
  const location = useLocation();
  const navigate = useNavigate();

  const handleSettingsIconClick = () => {
    const path = location.pathname === '/' ? '/settings' : '/';
    navigate(path);
  };

  return (
    <header className="flex items-center justify-between px-5 py-4">
      <div className="flex items-center gap-2">
        <div>{/* TODO: logo */}</div>

        <div>
          <h1 className="text-sm font-bold">Cogniflex</h1>
          <h2 className="text-2xs text-gray">ИИ-детектор</h2>
        </div>
      </div>
      <div className="flex items-center gap-3">
        <div className="group flex items-center gap-2">
          <div className="relative">
            <p className={twMerge('text-gray text-2xs', detectEnabled && 'text-white')}>
              {detectEnabled ? 'Активно' : 'Неактивно'}

              <div
                role="alert"
                className="bg-dark-blue text-2xs absolute top-6 left-[50%] hidden w-37 translate-x-[-50%] rounded-lg p-2 text-white group-hover:block"
              >
                Функция определения ИИ-контента на странице
              </div>
            </p>
          </div>
          <Switch checked={detectEnabled} onCheck={setDetectEnabled} />
        </div>
        <IconSettings
          onClick={handleSettingsIconClick}
          role="button"
          className="text-gray cursor-pointer"
          size={16}
        />
      </div>
    </header>
  );
};
