import { CommonModule } from '@angular/common';
import { Component, OnInit, HostListener } from '@angular/core';
import { RouterOutlet, Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';
import { LoginResponse } from '../types/types';
import { Book } from '../types/types';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  currentUser: LoginResponse | null = null;
  dropdownOpen = false;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      if (!user) {
        this.router.navigate(['/login']);
      }
    });
  }

  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
  }

  closeDropdown() {
    this.dropdownOpen = false;
  }

  logout() {
    this.authService.logout();
    this.dropdownOpen = false;
    this.router.navigate(['/login']);
  }

  // Sluit dropdown als je buiten klikt
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu')) {
      this.dropdownOpen = false;
    }
  }
}

//book mockups, delete when replaced with Book database
export const MOCK_BOOKS: Book[] = [
  { id: 1, title: 'De Ontdekking van de Hemel', tagIds: [] },
  { id: 2, title: 'Het Diner', tagIds: [] },
  { id: 3, title: 'Oorlogswinter', tagIds: [] },
  { id: 4, title: 'De Aanslag', tagIds: [] },
  { id: 5, title: 'Turks Fruit', tagIds: [] },
];
