import { AnalyzeKind, type TAnalyzeResult } from '@/entities/analyze';
import { ANALYZE_RESULTS_MAP } from '@/sections/analyze/components/analyze-result/constants';
import type { Nullable } from '@/types';
import { IconLoader2 } from '@tabler/icons-react';
import { twMerge } from 'tailwind-merge';

const KIND_TO_RESULT_CLASSES: Record<AnalyzeKind, Record<'container' | 'badge', string>> = {
  [AnalyzeKind.REAL]: {
    container: 'border-green bg-green/20',
    badge: 'text-green bg-green/20 border-green',
  },
  [AnalyzeKind.AI]: {
    container: 'border-red bg-red/20',
    badge: 'text-red bg-red/20 border-red',
  },
};

export type AnalyzeResultProps = {
  isAnalyzing?: boolean;
  analyzeResult?: Nullable<TAnalyzeResult>;
  error?: string;
};

export const AnalyzeResult = ({ isAnalyzing, error, analyzeResult }: AnalyzeResultProps) => {
  const getContent = () => {
    if (error) {
      return <span className="text-red text-xs font-semibold">{error}</span>;
    }

    if (isAnalyzing) {
      return (
        <div className="text-gray flex items-center gap-2">
          <IconLoader2 size={16} className="animate-spin" />
          <span className="text-xs font-semibold">Активно работаем...</span>
        </div>
      );
    }

    if (analyzeResult) {
      const classes = KIND_TO_RESULT_CLASSES[analyzeResult.kind];
      return (
        <div
          className={twMerge(
            'flex w-full items-start justify-between rounded-2xl border px-4 py-3',
            classes.container,
          )}
        >
          <div
            className={twMerge(
              'rounded-full border px-2 py-0.5 text-xs font-semibold',
              classes.badge,
            )}
          >
            {ANALYZE_RESULTS_MAP[analyzeResult.kind]}
          </div>

          <div className="flex flex-col items-center gap-0.5">
            <span className="text-3xl font-semibold">{analyzeResult.accuracy * 100}%</span>
            <span className="text-2xs text-gray">Уверенность</span>
          </div>
        </div>
      );
    }

    return <span className="text-gray text-xs font-semibold">Здесь будет результат</span>;
  };

  return <div className="flex justify-center px-5 py-4">{getContent()}</div>;
};
