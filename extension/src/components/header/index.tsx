import { Switch } from '@/components/ui';
import { IconSettings } from '@tabler/icons-react';
import { useState } from 'react';
import { twMerge } from 'tailwind-merge';

export const Header = () => {
  const [isActive, setIsActive] = useState(false);

  return (
    <header className="flex flex-row items-center justify-between px-5 py-4">
      <div className="flex flex-row items-center gap-2">
        <div>{/* TODO: logo */}</div>

        <div>
          <h1 className="text-sm font-bold">Cogniflex</h1>
          <h2 className="text-2xs text-gray">ИИ-детектор</h2>
        </div>
      </div>
      <div className="flex flex-row items-center gap-2">
        <p className={twMerge('text-gray text-2xs', isActive && 'text-white')}>
          {isActive ? 'Активно' : 'Неактивно'}
        </p>
        <Switch checked={isActive} onCheck={setIsActive} />
        <IconSettings className="text-gray" size={16} />
      </div>
    </header>
  );
};
