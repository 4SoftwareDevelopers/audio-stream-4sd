# Login Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement login screen for administrators with JWT authentication

**Architecture:** Angular standalone components with HttpClient. Token stored in localStorage. AuthGuard protects routes. Redirect to /voice-messages on success.

**Tech Stack:** Angular 21, HttpClient, PrimeNG

---

### Task 1: Create AuthConfig

**Files:**
- Create: `audio-stream-web-admin/src/app/core/config/auth.config.ts`

**Purpose:** Centralized API configuration

```typescript
import { ApplicationConfig } from '@angular/core';
import { provideHttpClient, withFetch } from '@angular/common/http';

export const authConfig = {
  apiBaseUrl: 'http://localhost:8080',
  loginEndpoint: '/auth/login',
  refreshEndpoint: '/auth/refresh',
  tokenKey: 'jwt_token',
  refreshTokenKey: 'refresh_token',
};
```

- [ ] **Step 1: Create the config file**

- [ ] **Step 2: Commit**

```bash
git add audio-stream-web-admin/src/app/core/config/auth.config.ts
git commit -m "feat: add auth config"
```

---

### Task 2: Create AuthService

**Files:**
- Create: `audio-stream-web-admin/src/app/core/services/auth.service.ts`
- Modify: `audio-stream-web-admin/src/app/app.config.ts` (add provideHttpClient)

**Purpose:** Handle authentication HTTP calls

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { authConfig } from '../config/auth.config';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  jwt: string;
  refreshToken: string | null;
  expiresIn: number;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${authConfig.apiBaseUrl}${authConfig.loginEndpoint}`,
      request
    ).pipe(
      tap(response => {
        localStorage.setItem(authConfig.tokenKey, response.jwt);
        if (response.refreshToken) {
          localStorage.setItem(authConfig.refreshTokenKey, response.refreshToken);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(authConfig.tokenKey);
    localStorage.removeItem(authConfig.refreshTokenKey);
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem(authConfig.tokenKey);
  }

  getToken(): string | null {
    return localStorage.getItem(authConfig.tokenKey);
  }
}
```

- [ ] **Step 1: Create AuthService**

- [ ] **Step 2: Update app.config.ts to add provideHttpClient**

```typescript
import { provideHttpClient } from '@angular/common/http';

// Add to providers array:
provideHttpClient(withFetch()),
```

- [ ] **Step 3: Commit**

---

### Task 3: Create AuthGuard

**Files:**
- Create: `audio-stream-web-admin/src/app/core/guards/auth.guard.ts`

**Purpose:** Protect routes from unauthenticated access

```typescript
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree(['/login']);
};
```

- [ ] **Step 1: Create AuthGuard**

- [ ] **Step 2: Commit**

---

### Task 4: Create LoginComponent

**Files:**
- Create: `audio-stream-web-admin/src/app/features/login/login.component.ts`
- Create: `audio-stream-web-admin/src/app/features/login/login.component.html`
- Create: `audio-stream-web-admin/src/app/features/login/login.component.css`

**Purpose:** Login UI

```typescript
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { AuthService } from '../../core/services/auth.service';
import { AppSettingsService } from '../../core/services/app-settings.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, PasswordModule, ButtonModule, MessageModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private authService = inject(AuthService);
  private appSettings = inject(AppSettingsService);
  private router = inject(Router);

  username = '';
  password = '';
  loading = false;
  error: string | null = null;

  onSubmit(): void {
    if (!this.username || !this.password) {
      this.error = 'Please enter username and password';
      return;
    }

    this.loading = true;
    this.error = null;

    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => {
        this.router.navigate(['/voice-messages']);
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Invalid username or password';
      },
    });
  }

  t(key: string): string {
    return this.appSettings.t(key);
  }
}
```

```html
<div class="login-container">
  <div class="login-card">
    <div class="login-header">
      <i class="pi pi-microphone logo-icon"></i>
      <h1>AudioStream</h1>
    </div>

    @if (error) {
      <p-message severity="error" [text]="error"></p-message>
    }

    <form (ngSubmit)="onSubmit()">
      <div class="form-field">
        <label for="username">{{ t('login.username') }}</label>
        <input
          pInputText
          id="username"
          [(ngModel)]="username"
          name="username"
          [placeholder]="t('login.username')"
        />
      </div>

      <div class="form-field">
        <label for="password">{{ t('login.password') }}</label>
        <p-password
          id="password"
          [(ngModel)]="password"
          name="password"
          [feedback]="false"
          [toggleMask]="true"
        ></p-password>
      </div>

      <button
        pButton
        type="submit"
        [label]="t('login.submit')"
        [loading]="loading"
        class="login-btn"
      ></button>
    </form>
  </div>
</div>
```

```css
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--p-surface-ground);
}

.login-card {
  background: var(--p-surface-card);
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-header h1 {
  margin: 0.5rem 0 0;
}

.logo-icon {
  font-size: 3rem;
  color: var(--p-primary-500);
}

.form-field {
  margin-bottom: 1.5rem;
}

.form-field label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-field input,
.form-field p-password {
  width: 100%;
}

.login-btn {
  width: 100%;
  margin-top: 1rem;
}

:host ::ng-deep .p-password {
  width: 100%;
}

:host ::ng-deep .p-password-input {
  width: 100%;
}
```

- [ ] **Step 1: Create login.component.ts**

- [ ] **Step 2: Create login.component.html**

- [ ] **Step 3: Create login.component.css**

- [ ] **Step 4: Commit**

---

### Task 5: Update Routes

**Files:**
- Modify: `audio-stream-web-admin/src/app/app.routes.ts`

**Purpose:** Add login route and protect existing routes

```typescript
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
```

- [ ] **Step 1: Update app.routes.ts**

- [ ] **Step 2: Commit**

---

### Task 6: Add Login Translations

**Files:**
- Modify: `audio-stream-web-admin/src/app/core/services/app-settings.service.ts`

**Purpose:** Add login translations

```typescript
// Add to translations object:
'login.username': { en: 'Username', es: 'Usuario' },
'login.password': { en: 'Password', es: 'Contraseña' },
'login.submit': { en: 'Login', es: 'Iniciar Sesión' },
```

- [ ] **Step 1: Add login translations**

- [ ] **Step 2: Commit**

---

### Task 7: Run and Verify

**Files:**
- Build and test manually

**Purpose:** Verify implementation

- [ ] **Step 1: Run `npm run build`**

- [ ] **Step 2: Start app `npm start`**

- [ ] **Step 3: Navigate to /login**

- [ ] **Step 4: Test login with valid credentials**

- [ ] **Step 5: Test login with invalid credentials shows error**

- [ ] **Step 6: Commit**

---

## Execution

**Plan complete and saved to `docs/superpowers/plans/2026-04-24-login-design.md`.**

Two execution options:

1. **Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

2. **Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints for review

**Which approach?**