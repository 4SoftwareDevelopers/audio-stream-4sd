import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { TranslationService } from '../../core/services/translation.service';

@Component({
  selector: 'app-voice-messages',
  imports: [CommonModule, CardModule, ButtonModule],
  providers: [TranslationService],
  templateUrl: './voice-messages.component.html',
  styleUrl: './voice-messages.component.css',
})
export class VoiceMessagesComponent {
  private translationService: TranslationService;

  constructor() {
    this.translationService = new TranslationService();
  }

  t(key: string): string {
    return this.translationService.t(key);
  }
}
