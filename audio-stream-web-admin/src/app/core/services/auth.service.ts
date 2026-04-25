import { Injectable, inject, signal } from '@angular/core';
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
  private _isAuthenticated = signal(!!localStorage.getItem(authConfig.tokenKey));

  isAuthenticated = this._isAuthenticated.asReadonly();

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${authConfig.apiBaseUrl}${authConfig.loginEndpoint}`,
      request
    ).pipe(
      tap(response => {
        localStorage.setItem(authConfig.tokenKey, response.jwt);
        this._isAuthenticated.set(true);
        if (response.refreshToken) {
          localStorage.setItem(authConfig.refreshTokenKey, response.refreshToken);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(authConfig.tokenKey);
    localStorage.removeItem(authConfig.refreshTokenKey);
    this._isAuthenticated.set(false);
  }

  getToken(): string | null {
    return localStorage.getItem(authConfig.tokenKey);
  }
}