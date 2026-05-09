import { useId, useRef, type ReactNode, type TextareaHTMLAttributes } from 'react';
import { twMerge } from 'tailwind-merge';

export type TextAreaProps = TextareaHTMLAttributes<HTMLTextAreaElement> &
  Partial<
    Record<'startAdornment' | 'endAdornment', (adornmentProps: { isError: boolean }) => ReactNode>
  > & {
    isError?: boolean;
    label?: string;
    helperText?: string;
    textAreaContainerClassName?: string;
  };

export const TextArea = ({
  startAdornment,
  endAdornment,
  className,
  textAreaContainerClassName,
  label,
  helperText,
  disabled = false,
  isError = false,
  id,
  ...rest
}: TextAreaProps) => {
  const textAreaRef = useRef<HTMLTextAreaElement>(null);

  const generatedId = useId();
  const textAreaId = id || generatedId;

  const handleContainerClick = () => textAreaRef.current?.focus();

  return (
    <div className={twMerge('flex min-h-20 flex-col gap-1', className)}>
      {label && (
        <label className="text-2xs font-medium" htmlFor={textAreaId}>
          {label}
        </label>
      )}
      <div
        onClick={handleContainerClick}
        className={twMerge(
          'bg-dark-blue border-gray/30 flex cursor-text gap-1 rounded-lg border px-3 py-2 transition-all focus-within:border-white',
          disabled && 'bg-gray/30 border-none',
          textAreaContainerClassName,
        )}
      >
        {startAdornment && startAdornment({ isError })}
        <textarea
          disabled={disabled}
          className="placeholder-gray no-scrollbar disabled:text-gray flex-1 resize-none text-xs outline-none"
          ref={textAreaRef}
          id={textAreaId}
          {...rest}
        />
        {endAdornment && endAdornment({ isError })}
      </div>
      {helperText && <span className="text-3xs text-gray font-medium">{helperText}</span>}
    </div>
  );
};
