import { AnalyzeKind } from '@/entities/analyze';

export const ANALYZE_RESULTS_MAP: Record<AnalyzeKind, string> = {
  [AnalyzeKind.REAL]: 'Оригинальный контент',
  [AnalyzeKind.AI]: 'Сгенерировано ИИ',
};
