import { ChangeDetectorRef, Component, inject, model } from '@angular/core';
import { Book, ConfirmDialogAction } from '../../../types/types';
import { Router } from '@angular/router';
import { PopUpComponent } from '../../pop-up/pop-up.component';
import { BookService } from '../../services/book-service';

@Component({
  selector: 'app-reading-list-slide-out-pop-up',
  imports: [PopUpComponent],
  templateUrl: './reading-list-slide-out-pop-up.html',
  styleUrl: './reading-list-slide-out-pop-up.css',
})
export class ReadingListSlideOutPopUp {
  readingListQueue = model<Book[]>([]);
  cdr = inject(ChangeDetectorRef)
  api = inject(BookService)
  router = inject(Router);
  popUpActions: ConfirmDialogAction[] = [{ label: 'Sluiten', type: 'cancel', value: 'cancel' }]
  showPopUp = model<Boolean>(false);

  removeBookFromQueue(book: Book) {
    if (this.readingListQueue().length > 1) {
      this.readingListQueue().splice(this.readingListQueue().indexOf(book),1)
      this.cdr.detectChanges();
    }
    else {
      this.clearQueue();
    }
  }

  makeReadingListWithBooks() {
    const url = this.router.serializeUrl(
      this.router.createUrlTree(["/lists/add"], {queryParams: {"ids": this.readingListQueue().map((book) => book.id).join(",")}})
    );
    window.open(url, '_blank');
  }

  clearQueue() {
    this.readingListQueue.set([]);
    this.cdr.detectChanges();
  }

  closePopUp() {
    this.showPopUp.set(false);
  }
}
