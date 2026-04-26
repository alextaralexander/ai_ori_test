import { resourcesEn } from './resources_en';
import { resourcesRu } from './resources_ru';

const dictionaries = {
  en: resourcesEn,
  ru: resourcesRu
};

export type I18nKey = keyof typeof resourcesRu;

export function t(key: string): string {
  const lang = navigator.language.toLowerCase().startsWith('en') ? 'en' : 'ru';
  const dictionary = dictionaries[lang];
  return dictionary[key as I18nKey] ?? resourcesRu[key as I18nKey] ?? key;
}
