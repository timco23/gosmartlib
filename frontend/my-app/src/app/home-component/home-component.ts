import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { Book, LoginResponse } from '../../types/types';
import { BookService } from '../services/book-service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home-component',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home-component.html',
  styleUrl: './home-component.css',
})
export class HomeComponent implements OnInit {
  cdr = inject(ChangeDetectorRef);

  currentUser: LoginResponse | null = null;
  featuredBooks: Book[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(
    private authService: AuthService,
    private bookService: BookService,
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });

    this.bookService.getFeaturedBooks().subscribe({
      next: (books) => {
        this.featuredBooks = books;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Kon uitgelichte boeken niet laden.';
        this.isLoading = false;
      },
    });
  }
}
