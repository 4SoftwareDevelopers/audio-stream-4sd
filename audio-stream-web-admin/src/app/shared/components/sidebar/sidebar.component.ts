import { Component, signal, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { TooltipModule } from 'primeng/tooltip';
import { AppSettingsService } from '../../../core/services/app-settings.service';
import { AuthService } from '../../../core/services/auth.service';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterModule, ButtonModule, MenuModule, TooltipModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  @Output() collapsedChange = new EventEmitter<boolean>();

  private appSettings = inject(AppSettingsService);
  private authService = inject(AuthService);
  private router = inject(Router);

  collapsed = signal(false);
  darkMode = this.appSettings.darkMode;

  constructor() {
    this.appSettings.translationService;
  }

  getLanguage() {
    return this.appSettings.translationService.currentLanguage;
  }

  toggleSidebar(): void {
    this.collapsed.update((v) => !v);
    this.collapsedChange.emit(this.collapsed());
  }

  toggleDarkMode(): void {
    this.appSettings.toggleDarkMode();
  }

  toggleLanguage(): void {
    this.appSettings.translationService.toggleLanguage();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  t(key: string): string {
    return this.appSettings.t(key);
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