import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { ReadingListService } from '../../services/readingList-service';
import { ReadingList, Book, Role } from '../../../types/types';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BookService } from '../../services/book-service';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-reading-list-detail-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './reading-list-detail-page.html',
  styleUrl: './reading-list-detail-page.css',
})
export class ReadingListDetailPage implements OnInit {

  loading: boolean = true;
  loadingError: boolean = false;
  readingList: ReadingList | null = null;
  scrollIndex: number = 0;
  isAnimating: boolean = true;
  isMoving: boolean = false;
  extendedItems: Book[] = [];

  readingListService = inject(ReadingListService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);
  bookService = inject(BookService);
  authService = inject(AuthService);
  role = Role;

  ngOnInit() {
    this.route.paramMap.subscribe((p) => {
      const id = p.get('id');
      if (!id) return;

      this.readingListService.getReadingListById(parseInt(id)).subscribe({
        next: (list) => {
          this.readingList = list;
          this.prepareExtendedItems();
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: () => {this.loadingError = true; this.cdr.detectChanges();}
      })
    })
  }

  private prepareExtendedItems() {
    if (!this.readingList || this.readingList.items.length === 0) {
      this.extendedItems = [];
      return;
    }
    const items = this.readingList.items;
    if (items.length < 6) {
      this.extendedItems = items;
      return;
    }
    // Add 5 clones at start and end for smooth looping
    this.extendedItems = [...items.slice(-5), ...items, ...items.slice(0, 5)];
  }

  get normalizedScrollIndex(): number {
    if (!this.readingList) return 0;
    const n = this.readingList.items.length;
    return (this.scrollIndex % n + n) % n;
  }

  get trackTransform(): string {
    const items = this.readingList?.items || [];
    const n = items.length;
    if (n < 6) return 'none';
    
    // Base index is 5 (due to clones)
    const displayIndex = this.scrollIndex + 5;
    
    // Center the card: Since track has left: 50%, 
    // we translate back by the distance from track start to card center.
    // Fixed gap of 32px to match CSS.
    return `translateX(calc(-1 * (${displayIndex} * (210px + 32px) + 105px)))`;
  }

  isCardActive(extendedIndex: number): boolean {
    if (!this.readingList || this.readingList.items.length < 6) return false;
    const n = this.readingList.items.length;
    const normalizedIndex = extendedIndex - 5;
    
    // Check if this card matches the scrollIndex OR its wrapped equivalents
    // This ensures clones are also highlighted when their original is at the focus
    return normalizedIndex === this.scrollIndex || 
           normalizedIndex === this.scrollIndex - n || 
           normalizedIndex === this.scrollIndex + n;
  }

  prevCard() {
    if (!this.readingList || this.isMoving) return;
    const n = this.readingList.items.length;
    
    if (n < 6) {
      this.scrollIndex = (this.scrollIndex - 1 + n) % n;
      return;
    }

    this.isMoving = true;
    this.scrollIndex--;
    
    // Check if we need to jump after this transition
    const needsJump = this.scrollIndex < 0;

    setTimeout(() => {
      if (needsJump) {
        this.isAnimating = false;
        this.scrollIndex = n - 1;
        this.cdr.detectChanges();
        
        // Force reflow
        const track = document.querySelector('.carousel-track') as HTMLElement;
        if (track) void track.offsetHeight;

        requestAnimationFrame(() => {
          requestAnimationFrame(() => {
            this.isAnimating = true;
            this.isMoving = false;
            this.cdr.detectChanges();
          });
        });
      } else {
        this.isMoving = false;
      }
    }, 550);
  }

  nextCard() {
    if (!this.readingList || this.isMoving) return;
    const n = this.readingList.items.length;
    
    if (n < 6) {
      this.scrollIndex = (this.scrollIndex + 1) % n;
      return;
    }

    this.isMoving = true;
    this.scrollIndex++;
    
    // Check if we need to jump after this transition
    const needsJump = this.scrollIndex >= n;

    setTimeout(() => {
      if (needsJump) {
        this.isAnimating = false;
        this.scrollIndex = 0;
        this.cdr.detectChanges();

        const track = document.querySelector('.carousel-track') as HTMLElement;
        if (track) void track.offsetHeight;

        requestAnimationFrame(() => {
          requestAnimationFrame(() => {
            this.isAnimating = true;
            this.isMoving = false;
            this.cdr.detectChanges();
          });
        });
      } else {
        this.isMoving = false;
      }
    }, 550);
  }

  goToSlide(index: number) {
    if (this.isMoving || !this.readingList) return;
    this.isMoving = true;
    this.scrollIndex = index;
    setTimeout(() => {
      this.isMoving = false;
    }, 500);
  }

  goToBookDetails(id: number) {
    this.router.navigate(['/books', id])
  }

  coverSrc(book: any): string | null {
    const id = Number(book?.id);
    return Number.isFinite(id) && id > 0 ? this.bookService.getBookCoverUrl(id) : null;
  }
}
