import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { AppSettingsService } from '../../core/services/app-settings.service';
import { Language, Theme } from '../../core/services/app-settings.service';

@Component({
  selector: 'app-settings',
  imports: [CommonModule, CardModule, ButtonModule, SelectModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css',
})
export class SettingsComponent {
  private appSettings = inject(AppSettingsService);

  selectedTheme = this.appSettings.theme;
  darkMode = this.appSettings.darkMode;

  languages = [
    { label: 'English', value: 'en' },
    { label: 'Español', value: 'es' },
  ];

  get selectedLanguage(): Language {
    return this.appSettings.getLanguage();
  }

  onLanguageChange(event: any): void {
    this.appSettings.setLanguage(event.value);
  }

  selectTheme(theme: Theme): void {
    this.appSettings.setTheme(theme);
  }

  t(key: string): string {
    return this.appSettings.t(key);
  }
}