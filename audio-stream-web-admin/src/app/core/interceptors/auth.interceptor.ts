import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { authConfig } from '../config/auth.config';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem(authConfig.tokenKey);

  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
    return next(cloned);
  }

  return next(req);
};