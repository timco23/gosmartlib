import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Book, SchoolItemCopy } from '../../../types/types';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-book-detail-page',
  imports: [RouterLink, FormsModule, CommonModule],
  templateUrl: './book-detail-page.html',
  styleUrl: './book-detail-page.css',
})
export class BookDetailPage implements OnInit {
  book: Book | null = null;
  api = inject(BookService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  copies: any[] = [];
  addingCopy: boolean = false;

  editingCopyId: number | null = null;
  editedCopy: SchoolItemCopy = { schoolItemId: 0, condition: '', barcode: '', copy_status: '' };

  newCopy: SchoolItemCopy = {
    schoolItemId: 0,
    condition: '',
    barcode: '',
    copy_status: ''
  };

  ngOnInit(): void {
    this.route.paramMap.subscribe(p => {
      const id = p.get("id")
      if (id) {
        this.getBook(id);
      }
    })
  }

  getBook(id: string) {
    const data = this.api.getBookById(parseInt(id));
    data.subscribe({
      next: (data) => {
        this.book = data;
        this.cdr.detectChanges();

        this.api.loadCopies(data.id).subscribe({
          next: (copies) => {
            this.copies = copies;
            this.cdr.detectChanges();
          }
        });
      }
    })
  }

  deleteBook(id: number) {
    this.api.deleteBook(id).subscribe({ next: () => this.backToList() });
  }

  backToList() {
    this.router.navigate(["/books"]);
  }

  addCopy() {
    if (!this.book) return;

    this.newCopy.schoolItemId = this.book.itemId;

    this.api.addCopy(this.newCopy).subscribe({
      next: (copy) => {
        this.copies.push(copy);

        this.newCopy = {
          schoolItemId: this.book!.itemId,
          condition: '',
          barcode: '',
          copy_status: ''
        };
        this.addingCopy = false;
        this.cdr.detectChanges();
      }
    });
  }

  cancelAddCopy() {
    this.addingCopy = false;
    this.newCopy = { schoolItemId: this.book!.itemId, condition: '', barcode: '', copy_status: '' };
  }

  showAddCopyForm() {
    this.addingCopy = true;
  }

  deleteCopy(copy: any) {
    this.api.deleteCopy(copy!).subscribe({
      next: () => {
        this.copies = []
        this.api.loadCopies(this.book!.id).subscribe({
          next: (copies) => {
            this.copies = copies;
            this.cdr.detectChanges();
          }
        });
        this.cdr.detectChanges();
      }
    });
  }

  startEdit(copy: SchoolItemCopy) {
  this.editingCopyId = copy.id!;
  this.editedCopy = { ...copy };
}

cancelEdit() {
  this.editingCopyId = null;
  this.editedCopy = { schoolItemId: 0, condition: '', barcode: '', copy_status: '' };
}

saveEdit() {
  if (!this.book) return;
  this.api.updateCopy(this.editedCopy).subscribe({
    next: (updatedCopy) => {
      const index = this.copies.findIndex(c => c.id === this.editingCopyId);
      if (index > -1) {
        this.copies[index] = updatedCopy;
      }
      this.editingCopyId = null;
      this.editedCopy = { schoolItemId: 0, condition: '', barcode: '', copy_status: '' };
      this.cdr.detectChanges();
    }
  });
}
}
