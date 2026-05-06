import type { z } from 'zod';
import type { settingsFormSchema } from './schema';

export type SettingsFormSchema = z.infer<typeof settingsFormSchema>;
