import { type InputHTMLAttributes } from 'react';
import { twMerge } from 'tailwind-merge';

export type SwitchProps = Pick<InputHTMLAttributes<HTMLInputElement>, 'checked' | 'className'> & {
  onCheck?(value: boolean): void;
};

export const Switch = ({ checked = false, onCheck, className }: SwitchProps) => {
  const handleClick = () => onCheck?.(!checked);

  return (
    <button
      role="switch"
      aria-checked={checked}
      className={twMerge(
        'relative h-5 w-10 cursor-pointer rounded-full p-0.5 transition-all',
        checked ? 'bg-accent' : 'bg-dark-blue',
        className,
      )}
      onClick={handleClick}
    >
      <div
        className={twMerge(
          'h-4 w-4 rounded-full bg-white transition-all',
          checked && 'translate-x-5',
        )}
      />
    </button>
  );
};
