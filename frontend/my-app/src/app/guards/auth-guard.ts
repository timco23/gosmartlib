import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AppRole, isAppRole } from './roles';
import { UserService } from '../services/user.service';
import { map } from 'rxjs';

const DEV_ROLE_BY_TOKEN: Record<string, AppRole> = {
  DEV_ADMIN_TOKEN: 'ADMIN',
  DEV_TEACHER_TOKEN: 'LEERKRACHT',
  DEV_BIBLIOTHEEKBEHEERDER_TOKEN: 'BIBLIOTHEEKBEHEERDER',
  DEV_STUDENT_TOKEN: 'LEERLING',
};

function normalizeRole(value: unknown): AppRole | null {
  if (typeof value !== 'string') return null;
  const upper = value.toUpperCase().trim();
  const normalized = upper.startsWith('ROLE_') ? upper.substring(5) : upper;
  return isAppRole(normalized) ? normalized : null;
}

function decodeBase64Url(input: string): string {
  const normalized = input.replace(/-/g, '+').replace(/_/g, '/');
  const padding = '='.repeat((4 - (normalized.length % 4)) % 4);
  return atob(normalized + padding);
}

function getRoleFromCurrentUser(authService: AuthService): AppRole | null {
  const user = authService.getCurrentUser() as { role?: unknown } | null;
  return normalizeRole(user?.role);
}

function getRoleFromToken(token: string): AppRole | null {
  const devRole = DEV_ROLE_BY_TOKEN[token];
  if (devRole) return devRole;

  try {
    const parts = token.split('.');
    if (parts.length < 2) return null;
    const payload = JSON.parse(decodeBase64Url(parts[1])) as {
      role?: unknown;
      roles?: unknown;
      authorities?: unknown;
    };

    const role = normalizeRole(payload.role);
    if (role) return role;

    const fromArray = (value: unknown): AppRole | null => {
      if (Array.isArray(value)) {
        for (const v of value) {
          const r = normalizeRole(v);
          if (r) return r;
        }
      }
      return null;
    };

    return (
      fromArray(payload.roles) ||
      fromArray(payload.authorities) ||
      normalizeRole(payload.roles) ||
      normalizeRole(payload.authorities)
    );
  } catch {
    return null;
  }
}

/**
 * Route guard that checks authentication and authorization.
 * - Redirects to /login if no token is present
 * - Determines user role from AuthService or JWT
 * - Verifies if the user has a school (redirects to /noschool if not)
 * - Verifies if the role is allowed for the route
 * - Redirects to /forbidden if access is denied
 */
export const authGuard: CanActivateFn = async (route, state) => {
  const authService = inject(AuthService);
  const userService = inject(UserService);
  const router = inject(Router);

  const token = authService.getToken();
  if (!token) {
    return router.createUrlTree(['/login'], { queryParams: { redirect: state.url } });
  }

  const user = authService.getCurrentUser();

  if (user?.role === 'ADMIN') {
    if (user.needsPasswordChange === true && !state.url.startsWith('/setup')) {
      return router.createUrlTree(['/setup']);
    }
    if (user.needsPasswordChange === false && state.url.startsWith('/setup')) {
      return router.createUrlTree(['/schools']);
    }
  }

  const allowedRoles = (route.data?.['roles'] ?? []) as AppRole[];
  if (allowedRoles.length === 0) return true;

  const role = getRoleFromCurrentUser(authService) ?? getRoleFromToken(token);
  if (!role) {
    return router.createUrlTree(['/login'], { queryParams: { redirect: state.url } });
  }

  const currentUser = authService.getCurrentUser();
  if (!currentUser?.email) {
    // If we have no user info (e.g. dev tokens), skip school check and just verify roles
    const allowedRoles = (route.data?.['roles'] ?? []) as AppRole[];
    if (allowedRoles.length === 0 || allowedRoles.includes(role)) {
      return true;
    }
    return router.createUrlTree(['/forbidden'], { queryParams: { from: state.url } });
  }

  return userService.hasSchool(currentUser.email).pipe(
    map((hasSchool) => {
      if (!hasSchool) {
        return router.createUrlTree(['/noschool']);
      }

      const allowedRoles = (route.data?.['roles'] ?? []) as AppRole[];
      if (allowedRoles.length === 0) return true;

      if (allowedRoles.includes(role)) {
        return true;
      }

      return router.createUrlTree(['/forbidden'], { queryParams: { from: state.url } });
    })
  );
};
