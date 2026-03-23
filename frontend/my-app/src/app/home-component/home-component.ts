import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { BookService } from '../services/book-service';
import { LoginResponse, Book, CarouselSlot, Tag } from '../../types/types';
import { TagService } from '../services/tag.service';

@Component({
  selector: 'app-home-component',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLink],
  templateUrl: './home-component.html',
  styleUrl: './home-component.css',
})
export class HomeComponent implements OnInit {
  private authService = inject(AuthService);
  private bookService = inject(BookService);
  private cdr = inject(ChangeDetectorRef);

  currentUser: LoginResponse | null = null;
  featuredBooks: Book[] = [];
  isLoading = true;
  error: string | null = null;
  scrollIndex = 0;
  
  tagService = inject(TagService);
  allTags: Tag[] = [];

  ngOnInit() {
    this.tagService.getTags().subscribe(tags => {
      this.allTags = tags;
    });

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

  mapTagIds(tagIds: number[]): Tag[] {
  return tagIds
    .map(id => this.allTags.find(t => t.id === id))
    .filter((t): t is Tag => !!t);
  }

  // Returns the cards that should be visible in the carousel
  // left cards + center + right cards
  get visibleSlots(): CarouselSlot[] {
    const n = this.featuredBooks.length;
    if (n === 0) return [];

    // one book on each side
    if (n === 3) {
      return [
        {
          book: this.featuredBooks[this.wrap(this.scrollIndex - 1)],
          index: this.wrap(this.scrollIndex - 1),
          position: 'left-1',
        },
        { book: this.featuredBooks[this.scrollIndex], index: this.scrollIndex, position: 'center' },
        {
          book: this.featuredBooks[this.wrap(this.scrollIndex + 1)],
          index: this.wrap(this.scrollIndex + 1),
          position: 'right-1',
        },
      ];
    }

    const slots: CarouselSlot[] = [];
    const usedIndices = new Set<number>([this.scrollIndex]);

    // Add 2 cards on the left
    const leftCards: CarouselSlot[] = [];
    for (let step = 1; step <= 2; step++) {
      const index = this.wrap(this.scrollIndex - step);
      if (usedIndices.has(index)) break;
      usedIndices.add(index);
      const position = step === 1 ? 'left-1' : 'left-2';
      leftCards.unshift({ book: this.featuredBooks[index], index: index, position });
    }

    // Add 2 cards on the right
    const rightCards: CarouselSlot[] = [];
    for (let step = 1; step <= 2; step++) {
      const index = this.wrap(this.scrollIndex + step);
      if (usedIndices.has(index)) break;
      usedIndices.add(index);
      const position = step === 1 ? 'right-1' : 'right-2';
      rightCards.push({ book: this.featuredBooks[index], index: index, position });
    }

    const centerCard: CarouselSlot = {
      book: this.featuredBooks[this.scrollIndex],
      index: this.scrollIndex,
      position: 'center',
    };
    return [...leftCards, centerCard, ...rightCards];
  }

  prevCard() {
    this.scrollIndex = this.wrap(this.scrollIndex - 1);
  }

  nextCard() {
    this.scrollIndex = this.wrap(this.scrollIndex + 1);
  }

  goToSlide(index: number) {
    this.scrollIndex = index;
  }

  // contains the index within the bounds of the books array
  private wrap(index: number): number {
    const length = this.featuredBooks.length;
    return ((index % length) + length) % length;
  }
}
