import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { TranslationService } from '../../core/services/translation.service';

interface BlockedContact {
  phone: string;
  name: string;
  blockedDate: string;
  reason: string;
}

@Component({
  selector: 'app-blacklist',
  imports: [CommonModule, CardModule, ButtonModule, TableModule, InputTextModule],
  providers: [TranslationService],
  templateUrl: './blacklist.component.html',
  styleUrl: './blacklist.component.css',
})
export class BlacklistComponent {
  private translationService: TranslationService;

  blockedContacts: BlockedContact[] = [
    { phone: '+1 555-0101', name: 'John Doe', blockedDate: '2024-01-15', reason: 'Spam' },
    { phone: '+1 555-0102', name: 'Jane Smith', blockedDate: '2024-01-20', reason: 'Harassment' },
    { phone: '+1 555-0103', name: 'Bob Wilson', blockedDate: '2024-02-01', reason: 'Scam' },
    { phone: '+1 555-0104', name: 'Alice Brown', blockedDate: '2024-02-10', reason: 'Spam' },
  ];

  constructor() {
    this.translationService = new TranslationService();
  }

  t(key: string): string {
    return this.translationService.t(key);
  }
}
