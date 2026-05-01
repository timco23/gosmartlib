import { ChangeDetectorRef, Component, inject, NgModule } from '@angular/core';
import { Book, Lending } from '../../types/types';
import { LendingService } from '../services/lending.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BookService } from '../services/book-service';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-book-lending-history',
  imports: [RouterLink, NgClass],
  templateUrl: './book-lending-history.component.html',
  styleUrl: './book-lending-history.component.css'
})
export class BookLendingHistoryComponent {
  api: LendingService = inject(LendingService);
  bookApi: BookService = inject(BookService);
  route: ActivatedRoute = inject(ActivatedRoute);
  cdr = inject(ChangeDetectorRef);

  lendings: Lending[] = [];
  failedCoverIds = new Set<number>();
  currentPage = 0;
  pageSize = 10;
  currentBook: any | null = null;

  get totalPages(): number {
    return Math.ceil(this.sortedLendings.length / this.pageSize);
  }

  get visibleLendings(): Lending[] {
    const start = this.currentPage * this.pageSize;
    return this.sortedLendings.slice(start, start + this.pageSize);
  }

  get sortedLendings(): Lending[] {
    const active = this.lendings
      .filter(l => !l.returned)
      .sort((a, b) => new Date(a.returnDate).getTime() - new Date(b.returnDate).getTime());

    const returned = this.lendings
      .filter(l => l.returned)
      .sort((a, b) => new Date(b.dateReturned!).getTime() - new Date(a.dateReturned!).getTime());

    return [...active, ...returned];
  }

  ngOnInit(): void {
    const bookId = Number(this.route.snapshot.paramMap.get('bookId'));

    this.bookApi.getBookById(bookId).subscribe((book: Book) => {
      this.currentBook = book;
    });
    
    this.api.getLendingsByBookId(bookId).subscribe((data: Lending[]) => {
      this.lendings = (data ?? []);
      this.currentPage = 0;
      this.cdr.detectChanges();
    });
    
    this.cdr.detectChanges();
  }

    nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.cdr.detectChanges();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.cdr.detectChanges();
    }
  }
}
