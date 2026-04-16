import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { TranslationService } from '../../core/services/translation.service';

type Language = 'en' | 'es';

@Component({
  selector: 'app-settings',
  imports: [CommonModule, CardModule, ButtonModule, SelectModule, FormsModule],
  providers: [TranslationService],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css',
})
export class SettingsComponent {
  private translationService: TranslationService;

  selectedTheme = signal<'light' | 'dark' | 'system'>('light');
  darkMode = signal(false);

  languages = [
    { label: 'English', value: 'en' },
    { label: 'Español', value: 'es' },
  ];

  selectedLanguage: Language = 'en';

  constructor() {
    this.translationService = new TranslationService();
    this.selectedLanguage = this.translationService.getLanguage();

    if (typeof localStorage !== 'undefined') {
      const saved = localStorage.getItem('darkMode');
      this.darkMode.set(saved === 'true');
    }
  }

  onLanguageChange(event: any): void {
    this.translationService.setLanguage(event.value);
    this.selectedLanguage = event.value;
  }

  selectTheme(theme: 'light' | 'dark' | 'system'): void {
    this.selectedTheme.set(theme);
    const isDark = theme === 'dark';
    this.darkMode.set(isDark);
    this.applyDarkMode(isDark);
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('darkMode', String(isDark));
    }
  }

  private applyDarkMode(isDark: boolean): void {
    if (typeof document !== 'undefined') {
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
  }

  t(key: string): string {
    return this.translationService.t(key);
  }
}
