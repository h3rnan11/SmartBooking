import { HttpInterceptorFn } from '@angular/common/http';

// Attaches the stored JWT to every outgoing request; the backend ignores it on public routes.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  if (!token) {
    return next(req);
  }
  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
