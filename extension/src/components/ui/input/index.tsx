import { useId, useRef, type InputHTMLAttributes, type ReactNode } from 'react';
import { twMerge } from 'tailwind-merge';

export type InputProps = InputHTMLAttributes<HTMLInputElement> &
  Partial<
    Record<'startAdornment' | 'endAdornment', (adornmentProps: { isError: boolean }) => ReactNode>
  > & {
    inputContainerClassName?: string;
    isError?: boolean;
    label?: string;
    helperText?: string;
  };

export const Input = ({
  startAdornment,
  endAdornment,
  className,
  inputContainerClassName,
  label,
  helperText,
  disabled = false,
  isError = false,
  id,
  ...rest
}: InputProps) => {
  const inputRef = useRef<HTMLInputElement>(null);

  const generatedId = useId();
  const inputId = id || generatedId;

  const handleContainerClick = () => inputRef.current?.focus();

  return (
    <div className={twMerge('flex flex-col gap-1', className)}>
      {label && (
        <label className="text-2xs font-medium" htmlFor={inputId}>
          {label}
        </label>
      )}
      <div
        onClick={handleContainerClick}
        className={twMerge(
          'bg-dark-blue border-gray/30 flex cursor-text flex-row items-center gap-1 rounded-lg border px-3 py-2 transition-all group-focus:border-white peer-focus:border-white focus-within:border-white',
          disabled && 'bg-gray/30 border-none',
          inputContainerClassName,
        )}
      >
        {startAdornment && startAdornment({ isError })}
        <input
          disabled={disabled}
          className="placeholder-gray disabled:text-gray text-xs outline-none"
          ref={inputRef}
          id={inputId}
          {...rest}
        />
        {endAdornment && endAdornment({ isError })}
      </div>
      {helperText && <span className="text-3xs text-gray font-medium">{helperText}</span>}
    </div>
  );
};
