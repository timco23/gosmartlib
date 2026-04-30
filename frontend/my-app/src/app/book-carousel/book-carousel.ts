import { Component, inject, Input, OnInit } from '@angular/core';
import { Book, CarouselSlot, Genre } from '../../types/types';
import { Router, RouterLink } from '@angular/router';
import { BookService } from '../services/book-service';
import { StarDisplayComponent } from '../widgets/star-display/star-display.component';

@Component({
  selector: 'app-book-carousel',
  imports: [StarDisplayComponent, RouterLink],
  templateUrl: './book-carousel.html',
  styleUrl: './book-carousel.css',
})
export class BookCarousel implements OnInit {
  @Input() books: Book[] = [];
  @Input() detailedView: boolean = false;
  @Input() scrollableThreshold: number = 5;
  @Input() autoScroll: boolean = false;
  @Input() progressDots: boolean = true;
  @Input() allGenres: Genre[] = [];
  autoScrollInterval: number = 5000;

  get scrollable(): boolean {
    return this.books.length >= this.scrollableThreshold;
  }
  scrollIndex: number = 0;
  router = inject(Router)
  bookService = inject(BookService)
  private autoScrollTimer?: any;

  ngOnInit() {
    this.startAutoScroll();
  }


  get visibleSlots(): CarouselSlot[] {
    if (!this.books || this.books.length === 0) return [];

    const n = this.books.length;

    if (n === 1) {
      return [{ book: this.books[0], index: 0, position: 'center' }];
    }

    if (n === 2) {
      return [
        { book: this.books[this.wrap(this.scrollIndex)], index: this.scrollIndex, position: 'center' },
        { book: this.books[this.wrap(this.scrollIndex + 1)], index: this.wrap(this.scrollIndex + 1), position: 'right-1' }
      ];
    }

    if (n === 3 || n === 4) {
      return [
        { book: this.books[this.wrap(this.scrollIndex - 1)], index: this.wrap(this.scrollIndex - 1), position: 'left-1' },
        { book: this.books[this.scrollIndex], index: this.scrollIndex, position: 'center' },
        { book: this.books[this.wrap(this.scrollIndex + 1)], index: this.wrap(this.scrollIndex + 1), position: 'right-1' }
      ];
    }

    const usedIndices = new Set<number>([this.scrollIndex]);

    // Add 2 cards on the left
    const leftCards: CarouselSlot[] = [];
    for (let step = 1; step <= 2; step++) {
      const index = this.wrap(this.scrollIndex - step);
      if (usedIndices.has(index)) break;
      usedIndices.add(index);
      const position = step === 1 ? 'left-1' : 'left-2';
      leftCards.unshift({ book: this.books[index], index: index, position });
    }

    // Add 2 cards on the right
    const rightCards: CarouselSlot[] = [];
    for (let step = 1; step <= 2; step++) {
      const index = this.wrap(this.scrollIndex + step);
      if (usedIndices.has(index)) break;
      usedIndices.add(index);
      const position = step === 1 ? 'right-1' : 'right-2';
      rightCards.push({ book: this.books[index], index: index, position });
    }

    const centerCard: CarouselSlot = {
      book: this.books[this.scrollIndex],
      index: this.scrollIndex,
      position: 'center',
    };
    return [...leftCards, centerCard, ...rightCards];
  }

  private wrap(index: number): number {
    if (!this.books) return 0;
    const length = this.books.length;
    return ((index % length) + length) % length;
  }

  prevCard() {
    this.scrollIndex = this.wrap(this.scrollIndex - 1);
    this.resetAutoScroll();
  }

  nextCard() {
    this.scrollIndex = this.wrap(this.scrollIndex + 1);
    this.resetAutoScroll();
  }

  goToSlide(index: number) {
    this.scrollIndex = index;
    this.resetAutoScroll();
  }

  goToBookDetails(id: number) {
    this.router.navigate(['/books', id])
  }

  coverSrc(book: Book): string | null {
    const id = book.id
    return this.bookService.getBookCoverUrl(id);
  }

  getSortedGenres(ids: number[]): Genre[] {
      return (ids || [])
        .map(id => this.allGenres.find(g => g.id === id)!)
        .sort((a, b) => a.name.localeCompare(b.name));
    }

  startAutoScroll() {
    if (this.autoScroll && this.scrollable) {
      this.stopAutoScroll();
      this.autoScrollTimer = setInterval(() => {
        this.scrollIndex = this.wrap(this.scrollIndex + 1);
      }, this.autoScrollInterval);
    }
  }

  stopAutoScroll() {
    if (this.autoScrollTimer) {
      clearInterval(this.autoScrollTimer);
      this.autoScrollTimer = undefined;
    }
  }

  private resetAutoScroll() {
    if (this.autoScrollTimer) {
      this.startAutoScroll();
    }
  }
}
