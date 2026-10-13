import { AnalyzeVerdict } from '@/entities/analyze';

export const en = {
  header: {
    subtitle: 'AI detector',
    active: 'Active',
    inactive: 'Inactive',
    tooltip: 'Detect AI-generated content on the page',
  },
  footer: {
    about: 'About',
    reportBug: 'Report a bug',
  },
  analyze: {
    title: 'Analysis',
    fileInfo: (extension: string, size: string) => `${extension} file • ${size}`,
    placeholder: 'Enter text or drop a file here...',
    charCount: (count: number) => `${count} ${count === 1 ? 'character' : 'characters'}`,
    run: 'Analyze',
    failed: 'Analysis failed. Please try again',
  },
  result: {
    analyzing: 'Analyzing...',
    confidence: 'Confidence',
    empty: 'The result will appear here',
    verdict: {
      [AnalyzeVerdict.HUMAN]: 'Human-written',
      [AnalyzeVerdict.AI]: 'AI-generated',
    } satisfies Record<AnalyzeVerdict, string>,
  },
  settings: {
    title: 'Settings',
    description: 'Configure AI content detection on the page',
    language: 'Language',
    languageAuto: 'Browser default',
    minTextLengthLabel: 'Minimum text length (chars)',
    minTextLengthPlaceholder: 'e.g. 5000',
    minTextLengthHelper: (min: number, def: number) => `Minimum: ${min}. Default: ${def}`,
    save: 'Save',
  },
  errors: {
    413: 'File is too large',
    415: 'This file type is not supported',
  },
  fileSizeUnits: ['B', 'KB', 'MB', 'GB', 'TB'],
};

export type Dictionary = typeof en;
