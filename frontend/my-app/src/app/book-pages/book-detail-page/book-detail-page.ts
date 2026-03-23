import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Book, Role, LoginResponse, SchoolItemCopy } from '../../../types/types';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-book-detail-page',
  imports: [RouterLink, FormsModule, CommonModule],
  templateUrl: './book-detail-page.html',
  styleUrl: './book-detail-page.css',
})
export class BookDetailPage implements OnInit {
  book: Book | null = null;
  currentUser: LoginResponse | null = null;
  api = inject(BookService);
  userApi = inject(UserService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  authService = inject(AuthService)
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  copies: any[] = [];
  addingCopy: boolean = false;
  role = Role;

  editingCopyId: number | null = null;
  editedCopy: SchoolItemCopy = { schoolItemId: 0, condition: '', barcode: '', copy_status: '' };
  isFavorite: boolean = false;

  newCopy: SchoolItemCopy = {
    schoolItemId: 0,
    condition: '',
    barcode: '',
    copy_status: 'Beschikbaar',
  };

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;

      // Only load favorites once we have a user
      this.route.paramMap.subscribe((p) => {
        const id = p.get('id');
        if (id) {
          this.getBook(id);

          this.userApi.getFavorites().subscribe({
            next: (favorites) => {
              this.isFavorite = favorites.some((fav: any) => fav.id === this.book?.itemId);
              this.cdr.detectChanges();
            }
          });
        }
      });
    });
  }

  addToFavorites() {
    console.log('isFavorite:', this.isFavorite);
    console.log('book:', this.book);
    console.log('currentUser:', this.currentUser);

    if (!this.isFavorite) {
      if (!this.book || !this.currentUser) {
        console.log('EARLY RETURN - book or currentUser is null');
        return;
      }
    }
    if(!this.isFavorite) {
      if (!this.book || !this.currentUser) return;
  
      this.api.addToFavorites(this.book.itemId, this.currentUser.email).subscribe({
        next: () => {
          this.isFavorite = true;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Probleem bij toevoegen:', err)
      });
    }
    else {
      this.api.removeFromFavorites(this.book!.itemId, this.currentUser!.email).subscribe({
        next: () => {
          this.isFavorite = false;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Probleem bij verwijderen:', err)
      });
    }
  }

  getBook(id: string) {
    const data = this.api.getBookById(parseInt(id));
    data.subscribe({
      next: (data) => {
        this.book = data;
        this.cdr.detectChanges();
        console.log(data)

        this.api.loadCopies(data.id).subscribe({
          next: (copies) => {
            this.copies = copies;
            this.cdr.detectChanges();
          },
        });
      },
    });
  }

  getTagNames(): string {
    return this.book?.tags.map((t) => t.name).join(', ') ?? '';
  }

  deleteBook(id: number) {
    this.api.deleteBook(id).subscribe({ next: () => this.backToList() });
  }

  backToList() {
    this.router.navigate(['/books']);
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
          copy_status: 'Beschikbaar',
        };
        this.addingCopy = false;
        this.cdr.detectChanges();
      },
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
        this.copies = [];
        this.api.loadCopies(this.book!.id).subscribe({
          next: (copies) => {
            this.copies = copies;
            this.cdr.detectChanges();
          },
        });
        this.cdr.detectChanges();
      },
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
        const index = this.copies.findIndex((c) => c.id === this.editingCopyId);
        if (index > -1) {
          this.copies[index] = updatedCopy;
        }
        this.editingCopyId = null;
        this.editedCopy = { schoolItemId: 0, condition: '', barcode: '', copy_status: '' };
        this.cdr.detectChanges();
      },
    });
  }
}