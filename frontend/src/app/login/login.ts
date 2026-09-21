import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { Auth } from '../services/auth';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  protected readonly mode = signal<'login' | 'register'>('login');
  protected readonly errorMessage = signal('');
  protected readonly loading = signal(false);

  protected readonly loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  protected readonly registerForm = this.fb.group({
    name: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  toggleMode(): void {
    this.mode.set(this.mode() === 'login' ? 'register' : 'login');
    this.errorMessage.set('');
  }

  submitLogin(): void {
    if (this.loginForm.invalid) {
      return;
    }
    this.loading.set(true);
    this.errorMessage.set('');
    this.auth.login(this.loginForm.getRawValue() as { email: string; password: string }).subscribe({
      next: (response) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        this.loading.set(false);
        this.router.navigateByUrl('');
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error ?? 'No se pudo iniciar sesión');
      },
    });
  }

  submitRegister(): void {
    if (this.registerForm.invalid) {
      return;
    }
    this.loading.set(true);
    this.errorMessage.set('');
    this.auth
      .register(this.registerForm.getRawValue() as { name: string; lastName: string; email: string; password: string })
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.mode.set('login');
          this.loginForm.patchValue({ email: this.registerForm.value.email ?? '' });
        },
        error: (err) => {
          this.loading.set(false);
          this.errorMessage.set(err.error ?? 'No se pudo completar el registro');
        },
      });
  }
}
