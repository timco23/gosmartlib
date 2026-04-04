import { Component, EventEmitter, inject, model, OnInit, Output } from '@angular/core';
import { BookService } from '../../services/book-service';
import { Observable } from 'rxjs';
import { Book } from '../../../types/types';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-select-book-popup',
  imports: [AsyncPipe],
  templateUrl: './select-book-popup.html',
  styleUrl: './select-book-popup.css',
})
export class SelectBookPopup implements OnInit{
  shown = model<boolean>(false);
  itemId= model<number>(0);
  bookService = inject(BookService)
  books: Observable<Book[]> | null = null;

  @Output() addBookEvent = new EventEmitter();

  closePopup() {
    this.shown.update(() => false);
  }

  ngOnInit(): void {
    this.books = this.bookService.getBooks()
  }

  selectBook() {
    this.addBookEvent.emit();
    this.closePopup()
  }
}
