/**
 * Возвращает правильную форму слова в зависимости от числительного.
 *
 * @param {number} number - Число для проверки.
 * @param {[string, string, string]} titles - Массив из трех форм слова: [1, 2, 5] (например: ['яблоко', 'яблока', 'яблок']).
 * @returns {string} Правильная форма слова.
 *
 * @example
 * const words: [string, string, string] = ['яблоко', 'яблока', 'яблок'];
 * pluralize(1, words);  // 'яблоко'
 * pluralize(3, words);  // 'яблока'
 * pluralize(5, words);  // 'яблок'
 * pluralize(21, words); // 'яблоко'
 */
export const pluralize = (number: number, titles: [string, string, string]): string => {
  const cases: number[] = [2, 0, 1, 1, 1, 2];
  const mod100: number = number % 100;
  const mod10: number = number % 10;
  const index = mod100 > 4 && mod100 < 20 ? 2 : cases[mod10 < 5 ? mod10 : 5];

  return titles[index];
};
