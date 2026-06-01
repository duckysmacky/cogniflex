export type TAnalyzeResult = {
  verdict: AnalyzeVerdict;
  confidence: number;
};

export const AnalyzeVerdict = {
  HUMAN: 'human',
  AI: 'ai',
} as const;
export type AnalyzeVerdict = (typeof AnalyzeVerdict)[keyof typeof AnalyzeVerdict];
