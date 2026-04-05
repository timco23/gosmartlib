import { ChangeDetectorRef, Component, effect, EventEmitter, inject, model, OnInit, Output, Renderer2 } from '@angular/core';
import { BookService } from '../../services/book-service';
import { Observable } from 'rxjs';
import { Book } from '../../../types/types';
import { AsyncPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-select-book-popup',
  imports: [AsyncPipe,FormsModule],
  templateUrl: './select-book-popup.html',
  styleUrl: './select-book-popup.css',
})
export class SelectBookPopup implements OnInit{
  shown = model<boolean>(false);
  itemId= model<number>(0);
  bookService = inject(BookService)
  cdr = inject(ChangeDetectorRef)
  renderer = inject(Renderer2)
  books: Observable<Book[]> | null = null;
  dropdownValue: string = "0"
  invalidChoiceError: boolean = false;

  //Locks scrolling for body of the entire page, not just this component.
  constructor() {
    effect(() => {
      if (this.shown()) {
        this.renderer.addClass(document.body, 'no-scroll');
      } else {
        this.renderer.removeClass(document.body, 'no-scroll');
      }
    });
  }

  @Output() addBookEvent = new EventEmitter();

  closePopup() {
    this.shown.update(() => false);
  }

  ngOnInit(): void {
    this.books = this.bookService.getBooks()
  }

  selectBook() {
    this.invalidChoiceError = false;
    this.cdr.detectChanges();
    if (this.dropdownValue == "0" || this.itemId() == 0) {
      this.invalidChoiceError = true;
      this.cdr.detectChanges();
      return;
    }
    this.addBookEvent.emit();
    this.closePopup()
    this.dropdownValue = "0";
  }

  changeItemId() {
    this.itemId.update(() => parseInt(this.dropdownValue))
  }
}
