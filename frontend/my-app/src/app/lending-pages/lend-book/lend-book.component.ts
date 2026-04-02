import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Book, SchoolItemCopy } from '../../../types/types';

@Component({
  selector: 'app-lend-book',
  imports: [RouterLink],
  templateUrl: './lend-book.component.html',
  styleUrl: './lend-book.component.css'
})
export class LendBookComponent {
  private bookService: BookService;

  private currentBook: SchoolItemCopy | null = null;

  constructor(bookService: BookService) {
    this.bookService = bookService;
  }

  public ngOnInit(): void {
    this.bookService.getByBarcode('BC000001').subscribe((copy) => {
      this.currentBook = copy;
    });
  }
}
