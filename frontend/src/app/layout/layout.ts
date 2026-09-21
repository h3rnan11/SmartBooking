import { Component, signal } from '@angular/core';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-layout',
  standalone: true,
  templateUrl: './layout.html',
  styleUrl: './layout.scss',
})
export class Layout {

  activeItem = signal<string>('profile');
  isHamburgerOpen = signal<boolean>(false);

  selectItem(item: string): void {
    this.activeItem.set(item);
  }

  toggleHamburger(): void {
    this.isHamburgerOpen.update(open => !open);
  }

}


