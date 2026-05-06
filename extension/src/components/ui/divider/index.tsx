import { twMerge } from 'tailwind-merge';

export type DividerProps = {
  className?: string;
};

export const Divider = ({ className }: DividerProps) => {
  return <hr className={twMerge('text-gray/20 h-px w-full', className)} />;
};
