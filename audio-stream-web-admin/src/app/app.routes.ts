import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'voice-messages',
    loadComponent: () =>
      import('./features/voice-messages/voice-messages.component').then(m => m.VoiceMessagesComponent),
    canActivate: [authGuard],
  },
  {
    path: 'blacklist',
    loadComponent: () =>
      import('./features/blacklist/blacklist.component').then(m => m.BlacklistComponent),
    canActivate: [authGuard],
  },
  {
    path: 'settings',
    loadComponent: () =>
      import('./features/settings/settings.component').then(m => m.SettingsComponent),
    canActivate: [authGuard],
  },
  {
    path: '',
    redirectTo: 'voice-messages',
    pathMatch: 'full',
  },
];