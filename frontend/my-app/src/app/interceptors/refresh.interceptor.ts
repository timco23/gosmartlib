import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap } from 'rxjs/operators';
import { throwError } from 'rxjs';

/**
 * Interceptor that handles JWT token refresh.
 * If a request returns 401 (Unauthorized), tries to refresh the JWT using the refresh token.
 * If refresh succeeds, retries the original request.
 * If refresh fails, logs out the user.
 */
export const refreshInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error) => {
      // Only handle 401 Unauthorized
      if (error.status === 401) {
        // Try to refresh the token
        return authService.refreshToken().pipe(
          switchMap((response) => {
            // Token refreshed successfully, retry the original request
            const newToken = response.token;
            const newReq = req.clone({
              setHeaders: { Authorization: `Bearer ${newToken}` },
            });
            return next(newReq);
          }),
          catchError((refreshError) => {
            // Refresh failed, log out the user
            authService.logout();
            return throwError(() => refreshError);
          }),
        );
      }

      return throwError(() => error);
    }),
  );
};
