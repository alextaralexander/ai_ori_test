import { resourcesEn } from './resources_en';
import { resourcesRu } from './resources_ru';

const dictionaries = {
  en: resourcesEn,
  ru: resourcesRu
};

export type I18nKey = keyof typeof resourcesRu;

export function getCurrentLocale(): 'ru' | 'en' {
  const stored = window.localStorage.getItem('bestorigin.locale');
  if (stored === 'ru' || stored === 'en') {
    return stored;
  }
  return navigator.language.toLowerCase().startsWith('en') ? 'en' : 'ru';
}

export function setCurrentLocale(locale: 'ru' | 'en'): void {
  window.localStorage.setItem('bestorigin.locale', locale);
}

export function t(key: string): string {
  const lang = getCurrentLocale();
  const dictionary = dictionaries[lang];
  return dictionary[key as I18nKey] ?? resourcesRu[key as I18nKey] ?? key;
}
