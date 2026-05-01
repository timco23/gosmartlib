import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Book, Lending } from '../../types/types';
import { LendingService } from '../services/lending.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BookService } from '../services/book-service';

@Component({
  selector: 'app-book-lending-history',
  imports: [RouterLink],
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
    return Math.ceil(this.lendings.length / this.pageSize);
  }

  get visibleLendings(): Lending[] {
    const start = this.currentPage * this.pageSize;
    return this.lendings.slice(start, start + this.pageSize);
  }

  ngOnInit(): void {
    const bookId = Number(this.route.snapshot.paramMap.get('bookId'));

    this.currentBook = this.bookApi.getBookById(bookId);
    
    this.api.getLendingsByBookId(bookId).subscribe((data: Lending[]) => {
      this.lendings = (data ?? []);
      this.currentPage = 0;
      this.cdr.detectChanges();
    });
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
