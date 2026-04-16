import { Component, signal, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { TooltipModule } from 'primeng/tooltip';
import { TranslationService } from '../../../core/services/translation.service';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterModule, ButtonModule, MenuModule, TooltipModule],
  providers: [TranslationService],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  @Output() collapsedChange = new EventEmitter<boolean>();

  collapsed = signal(false);
  darkMode = signal(false);

  private translationService: TranslationService;

  constructor() {
    this.translationService = new TranslationService();
    if (typeof localStorage !== 'undefined') {
      const saved = localStorage.getItem('darkMode');
      this.darkMode.set(saved === 'true');
      this.applyDarkMode();
    }
  }

  getLanguage() {
    return this.translationService.currentLanguage;
  }

  toggleSidebar(): void {
    this.collapsed.update((v) => !v);
    this.collapsedChange.emit(this.collapsed());
  }

  toggleDarkMode(): void {
    const newValue = !this.darkMode();
    this.darkMode.set(newValue);
    this.applyDarkMode();
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('darkMode', String(newValue));
    }
  }

  private applyDarkMode(): void {
    if (typeof document !== 'undefined') {
      const html = document.documentElement;
      const body = document.body;
      if (this.darkMode()) {
        html.classList.add('dark');
        body.classList.add('dark');
      } else {
        html.classList.remove('dark');
        body.classList.remove('dark');
      }
    }
  }

  toggleLanguage(): void {
    this.translationService.toggleLanguage();
  }

  t(key: string): string {
    return this.translationService.t(key);
  }

  get menuItems(): MenuItem[] {
    return [
      {
        label: this.t('sidebar.voiceMessages'),
        icon: 'pi pi-microphone',
        routerLink: '/voice-messages',
      },
      {
        label: this.t('sidebar.blacklist'),
        icon: 'pi pi-ban',
        routerLink: '/blacklist',
      },
      {
        label: this.t('sidebar.settings'),
        icon: 'pi pi-cog',
        routerLink: '/settings',
      },
    ];
  }
}
