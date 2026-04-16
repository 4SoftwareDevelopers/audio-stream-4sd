import { Routes } from '@angular/router';
import { VoiceMessagesComponent } from './features/voice-messages/voice-messages.component';
import { BlacklistComponent } from './features/blacklist/blacklist.component';
import { SettingsComponent } from './features/settings/settings.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'voice-messages',
    pathMatch: 'full',
  },
  {
    path: 'voice-messages',
    component: VoiceMessagesComponent,
  },
  {
    path: 'blacklist',
    component: BlacklistComponent,
  },
  {
    path: 'settings',
    component: SettingsComponent,
  },
  {
    path: '**',
    redirectTo: 'voice-messages',
  },
];
