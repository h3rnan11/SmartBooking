import { Component, inject, signal } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

import { Auth } from '../services/auth';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './layout.html',
  styleUrl: './layout.scss',
})
export class Layout {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  activeItem = signal<string>('profile');
  isHamburgerOpen = signal<boolean>(false);

  selectItem(item: string): void {
    this.activeItem.set(item);
  }

  toggleHamburger(): void {
    this.isHamburgerOpen.update(open => !open);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }

}


