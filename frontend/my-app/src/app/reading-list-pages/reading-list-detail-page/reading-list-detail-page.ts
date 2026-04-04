import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { ReadingListService } from '../../services/readingList-service';
import { ReadingList, CarouselSlot, Book } from '../../../types/types';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reading-list-detail-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reading-list-detail-page.html',
  styleUrl: './reading-list-detail-page.css',
})
export class ReadingListDetailPage implements OnInit {

  loading: boolean = true;
  loadingError: boolean = false;
  readingList: ReadingList | null = null;
  scrollIndex: number = 0;

  readingListService = inject(ReadingListService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);

  ngOnInit() {
    this.route.paramMap.subscribe((p) => {
      const id = p.get('id');
      if (!id) return;

      this.readingListService.getReadingListById(parseInt(id)).subscribe({
        next: (list) => {
          this.readingList = list;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: () => this.loadingError = true
      })
    })
  }

  get visibleSlots(): CarouselSlot[] {
    if (!this.readingList || !this.readingList.items || this.readingList.items.length === 0) return [];
    
    const books = this.readingList.items;
    const n = books.length;

    if (n === 1) {
      return [{ book: books[0], index: 0, position: 'center' }];
    }

    if (n === 2) {
      return [
        { book: books[this.wrap(this.scrollIndex)], index: this.scrollIndex, position: 'center' },
        { book: books[this.wrap(this.scrollIndex + 1)], index: this.wrap(this.scrollIndex + 1), position: 'right-1' }
      ];
    }

    if (n === 3 || n === 4) {
      return [
        { book: books[this.wrap(this.scrollIndex - 1)], index: this.wrap(this.scrollIndex - 1), position: 'left-1' },
        { book: books[this.scrollIndex], index: this.scrollIndex, position: 'center' },
        { book: books[this.wrap(this.scrollIndex + 1)], index: this.wrap(this.scrollIndex + 1), position: 'right-1' }
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
      leftCards.unshift({ book: books[index], index: index, position });
    }

    // Add 2 cards on the right
    const rightCards: CarouselSlot[] = [];
    for (let step = 1; step <= 2; step++) {
      const index = this.wrap(this.scrollIndex + step);
      if (usedIndices.has(index)) break;
      usedIndices.add(index);
      const position = step === 1 ? 'right-1' : 'right-2';
      rightCards.push({ book: books[index], index: index, position });
    }

    const centerCard: CarouselSlot = {
      book: books[this.scrollIndex],
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

  private wrap(index: number): number {
    if (!this.readingList || !this.readingList.items) return 0;
    const length = this.readingList.items.length;
    return ((index % length) + length) % length;
  }

  goToBookDetails(id: number) {
    this.router.navigate(['/books', id])
  }
}
