import { signal, computed } from '@angular/core';

export type Language = 'en' | 'es';
export type Theme = 'light' | 'dark' | 'system';

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
  'login.username': { en: 'Username', es: 'Usuario' },
  'login.password': { en: 'Password', es: 'Contraseña' },
  'login.submit': { en: 'Login', es: 'Iniciar Sesión' },
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

export class AppSettingsService {
  private _translationService: TranslationService;

  readonly theme = signal<Theme>(this.loadTheme());
  readonly darkMode = computed(() => this.resolveDarkMode());

  constructor() {
    this._translationService = new TranslationService();
    this.initListeners();
  }

  private loadTheme(): Theme {
    if (typeof localStorage !== 'undefined') {
      const saved = localStorage.getItem('theme') as Theme;
      if (saved) return saved;
    }
    return 'light';
  }

  private resolveDarkMode(): boolean {
    const currentTheme = this.theme();
    if (currentTheme === 'system') {
      if (typeof window !== 'undefined') {
        return window.matchMedia('(prefers-color-scheme: dark)').matches;
      }
      return false;
    }
    return currentTheme === 'dark';
  }

  private initListeners(): void {
    if (typeof window !== 'undefined') {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
      mediaQuery.addEventListener('change', () => {
        if (this.theme() === 'system') {
          this.applyTheme();
        }
      });

      if (typeof localStorage !== 'undefined') {
        const savedDark = localStorage.getItem('darkMode');
        if (savedDark !== null) {
          localStorage.removeItem('darkMode');
          const newTheme = savedDark === 'true' ? 'dark' : 'light';
          this.theme.set(newTheme);
          this.applyTheme();
        }
      }
    }
  }

  private applyTheme(): void {
    if (typeof document === 'undefined') return;

    const isDark = this.darkMode();
    const html = document.documentElement;
    const body = document.body;

    if (isDark) {
      html.classList.add('dark');
      body.classList.add('dark');
    } else {
      html.classList.remove('dark');
      body.classList.remove('dark');
    }
  }

  setTheme(theme: Theme): void {
    this.theme.set(theme);
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('theme', theme);
    }
    this.applyTheme();
  }

  toggleDarkMode(): void {
    const currentTheme = this.theme();
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    this.setTheme(newTheme);
  }

  setLanguage(lang: Language): void {
    this._translationService.setLanguage(lang);
  }

  t(key: string): string {
    return this._translationService.t(key);
  }

  getLanguage(): Language {
    return this._translationService.getLanguage();
  }

  get translationService(): TranslationService {
    return this._translationService;
  }
}