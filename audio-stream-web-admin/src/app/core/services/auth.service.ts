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