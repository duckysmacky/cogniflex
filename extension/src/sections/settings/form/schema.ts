import z from 'zod';
import { MIN_TEXT_LENGTH_MIN } from './constants';

export const settingsFormSchema = z.object({
  minTextLength: z.coerce.number<number>().min(MIN_TEXT_LENGTH_MIN),
});
