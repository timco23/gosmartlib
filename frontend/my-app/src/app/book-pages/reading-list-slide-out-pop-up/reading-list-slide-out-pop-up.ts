import { ChangeDetectorRef, Component, inject, model } from '@angular/core';
import { Book } from '../../../types/types';
import { Router } from '@angular/router';

@Component({
  selector: 'app-reading-list-slide-out-pop-up',
  imports: [],
  templateUrl: './reading-list-slide-out-pop-up.html',
  styleUrl: './reading-list-slide-out-pop-up.css',
})
export class ReadingListSlideOutPopUp {
  readingListQueue = model<Book[]>([]);
  cdr = inject(ChangeDetectorRef)
  router = inject(Router);

  removeBookFromQueue(book: Book) {
    this.readingListQueue.update((oldQueue) => oldQueue.splice(this.readingListQueue().indexOf(book),1))
    this.cdr.detectChanges();
  }

  makeReadingListWithBooks() {
    this.router.navigate(["/lists/add"], {queryParams: {"ids": this.readingListQueue().map((book) => book.id).join(",")}})
  }

  clearQueue() {
    this.readingListQueue.set([]);
    this.cdr.detectChanges();
  }
}
