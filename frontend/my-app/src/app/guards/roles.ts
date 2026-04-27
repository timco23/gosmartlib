export type AppRole = 'LEERLING' | 'LEERKRACHT' | 'BIBLIOTHEEKBEHEERDER' | 'ADMIN';

export const ALL_USERS: AppRole[] = ['LEERLING', 'LEERKRACHT', 'BIBLIOTHEEKBEHEERDER', 'ADMIN'];
export const TEACHER_PLUS: AppRole[] = ['LEERKRACHT', 'BIBLIOTHEEKBEHEERDER', 'ADMIN'];
export const LIBRARIAN_PLUS: AppRole[] = ['BIBLIOTHEEKBEHEERDER', 'ADMIN'];
export const ADMIN_ONLY: AppRole[] = ['ADMIN'];

export function isAppRole(value: unknown): value is AppRole {
  return (
    value === 'LEERLING' ||
    value === 'LEERKRACHT' ||
    value === 'BIBLIOTHEEKBEHEERDER' ||
    value === 'ADMIN'
  );
}
