export type TAnalyzeResult = {
  kind: AnalyzeKind;
  accuracy: number;
};

export const AnalyzeKind = {
  REAL: 0,
  AI: 1,
} as const;
export type AnalyzeKind = (typeof AnalyzeKind)[keyof typeof AnalyzeKind];
