import { AnalyzeVerdict } from '@/entities/analyze';

export const ANALYZE_RESULTS_MAP: Record<AnalyzeVerdict, string> = {
  [AnalyzeVerdict.HUMAN]: 'Оригинальный контент',
  [AnalyzeVerdict.AI]: 'Сгенерировано ИИ',
};
