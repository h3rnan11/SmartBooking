import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Auth } from '../services/auth';

// Lets the navigation through only with a valid, unexpired token; otherwise sends the user to login.
export const authGuard: CanActivateFn = () => {
  const auth = inject(Auth);
  return auth.isAuthenticated() || inject(Router).createUrlTree(['/login']);
};

// Keeps a logged-in user from seeing the login page again.
export const guestGuard: CanActivateFn = () => {
  const auth = inject(Auth);
  return !auth.isAuthenticated() || inject(Router).createUrlTree(['/home']);
};
