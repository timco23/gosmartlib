import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book-service';
import { Book, SchoolItemCopy } from '../../../types/types';

@Component({
  selector: 'app-lend-book',
  imports: [RouterLink, FormsModule],
  templateUrl: './lend-book.component.html',
  styleUrl: './lend-book.component.css'
})
export class LendBookComponent {
  public currentSchoolCopy: SchoolItemCopy | null = null;
  public currentBook: Book | null = null;
  public barcode: string = '';
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  constructor(private bookService: BookService) { }

  onBarcodeChange(barcode: string): void {
    if (!barcode.trim()) {
      this.currentBook = null;
      this.currentSchoolCopy = null;
      this.cdr.detectChanges();
      return;
    }

    this.bookService.getByBarcode(barcode).subscribe({
      next: (copy) => {
        if (!copy) {
          this.currentSchoolCopy = null;
          this.currentBook = null;
          this.cdr.detectChanges();
          return;
        }

        this.currentSchoolCopy = copy;

        this.bookService.getBookById(copy.itemId).subscribe({
          next: (book) => {
            this.currentBook = book ?? null;
            this.cdr.detectChanges();
          },
          error: () => {
            this.currentBook = null;
            this.cdr.detectChanges();
          }
        });
      },
      error: () => {
        this.currentSchoolCopy = null;
        this.currentBook = null;
        this.cdr.detectChanges();
      }
    });
  }
}