import type { ButtonHTMLAttributes } from 'react';
import { twMerge } from 'tailwind-merge';

export type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {};

export const Button = ({ className, ...rest }: ButtonProps) => {
  return (
    <button
      className={twMerge(
        'bg-accent disabled:bg-gray/30 disabled:text-gray flex flex-row items-center justify-center rounded-lg px-3 py-2 text-xs font-semibold text-white transition-all',
        className,
      )}
      {...rest}
    />
  );
};
