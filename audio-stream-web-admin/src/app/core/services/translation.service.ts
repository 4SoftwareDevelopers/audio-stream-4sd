import { signal } from '@angular/core';

export type Language = 'en' | 'es';

const translations: Record<string, Record<string, string>> = {
  'sidebar.voiceMessages': { en: 'Voice Messages', es: 'Mensajes de Voz' },
  'sidebar.blacklist': { en: 'Blacklist', es: 'Lista Negra' },
  'sidebar.settings': { en: 'Settings', es: 'Configuración' },
  'sidebar.toggle': { en: 'Toggle Sidebar', es: 'Alternar Sidebar' },
  'header.darkMode': { en: 'Dark Mode', es: 'Modo Oscuro' },
  'header.lightMode': { en: 'Light Mode', es: 'Modo Claro' },
  'header.language': { en: 'Language', es: 'Idioma' },
  'voiceMessages.title': { en: 'Voice Messages', es: 'Mensajes de Voz' },
  'voiceMessages.description': {
    en: 'Manage your voice messages',
    es: 'Administra tus mensajes de voz',
  },
  'blacklist.title': { en: 'Blacklist', es: 'Lista Negra' },
  'blacklist.description': { en: 'Manage blocked contacts', es: 'Administra contactos bloqueados' },
  'settings.title': { en: 'Settings', es: 'Configuración' },
  'settings.description': { en: 'Application settings', es: 'Configuración de la aplicación' },
  'settings.language': { en: 'Language', es: 'Idioma' },
  'settings.theme': { en: 'Theme', es: 'Tema' },
};

export class TranslationService {
  readonly currentLanguage = signal<Language>('en');

  setLanguage(lang: Language): void {
    this.currentLanguage.set(lang);
  }

  toggleLanguage(): void {
    this.currentLanguage.update((lang) => (lang === 'en' ? 'es' : 'en'));
  }

  t(key: string): string {
    const lang = this.currentLanguage();
    return translations[key]?.[lang] || key;
  }

  getLanguage(): Language {
    return this.currentLanguage();
  }
}
