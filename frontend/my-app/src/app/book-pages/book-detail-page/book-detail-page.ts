import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Book, Role, LoginResponse, SchoolItemCopy, School, CreateSchoolItemCopyPayload } from '../../../types/types';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { SchoolService } from '../../services/school.service';

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
  schoolService = inject(SchoolService)
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  copies: SchoolItemCopy[] = [];
  addingCopy: boolean = false;
  role = Role;
  copyError: boolean = false;
  school: School | null = null;
  isFeatured: boolean = false;
  deleteError: boolean = false;

  editingCopyId: number | null = null;
  editedCopy: CreateSchoolItemCopyPayload = { itemId: 0, condition: "", barcode: '', copy_status: "", featured: false, schoolId: 0, campusId: 0 };
  isFavorite: boolean = false;

  newCopy: CreateSchoolItemCopyPayload = {
    itemId: 0,
    condition: "",
    barcode: '',
    copy_status: "BESCHIKBAAR",
    featured: false,
    schoolId: 0,
    campusId: 0
  };

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;

      // Only load favorites once we have a user
      this.route.paramMap.subscribe((p) => {
        const id = p.get('id');
        if (id) {
          this.userApi.getUser(user!.email).subscribe({
            next: (data) => {
              if (data.schoolName != null) {
                this.schoolService.getByName(data.schoolName).subscribe({
                  next: (data) => {
                    this.school = data;
                    this.getBook(id);
                  }
                })
              }
            }
          })

          this.userApi.getFavorites().subscribe({
            next: (favorites) => {
              this.isFavorite = favorites.some((fav: any) => fav.id === this.book?.id);
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
    if (!this.isFavorite) {
      if (!this.book || !this.currentUser) return;

      this.api.addToFavorites(this.book.id, this.currentUser.email).subscribe({
        next: () => {
          this.isFavorite = true;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Probleem bij toevoegen:', err)
      });
    }
    else {
      this.api.removeFromFavorites(this.book!.id, this.currentUser!.email).subscribe({
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

        this.api.loadCopies(data.id, this.school!.id!).subscribe({
          next: (copies) => {
            this.copies = copies;
            if (copies[0].featured) this.isFeatured = true;
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
    this.deleteError = false;
    this.cdr.detectChanges();
    this.api.deleteBook(id).subscribe({ next: () => this.backToList(), error: () => {this.deleteError = true ;this.cdr.detectChanges()} });
  }

  backToList() {
    this.router.navigate(['/books']);
  }

  addCopy() {
    if (!this.book) return;
    if (this.newCopy.condition == "") {
      this.copyError = true
      return;
    }

    this.newCopy.itemId = this.book.id;
    this.newCopy.schoolId = this.school!.id!;
    this.newCopy.featured = this.isFeatured;

    this.api.addCopy(this.newCopy).subscribe({
      next: (copy) => {
        this.copies.push(copy);

        this.newCopy = {
          itemId: this.book!.id,
          condition: "",
          barcode: '',
          copy_status: "BESCHIKBAAR",
          featured: false,
          schoolId: 0,
          campusId: 0
        };
        this.addingCopy = false;
        this.cdr.detectChanges();
      },
    });
  }

  cancelAddCopy() {
    this.addingCopy = false;
    this.newCopy = { itemId: this.book!.id, condition: "", barcode: '', copy_status: "BESCHIKBAAR", featured: false, schoolId: 0, campusId: 0 };
  }

  showAddCopyForm() {
    this.addingCopy = true;
  }

  deleteCopy(copy: any) {
    this.api.deleteCopy(copy!).subscribe({
      next: () => {
        this.copies = [];
        this.api.loadCopies(this.book!.id, this.school!.id!).subscribe({
          next: (copies) => {
            this.copies = copies;
            if (copies.length <= 0) {
              this.backToList();
            }
            this.cdr.detectChanges();
          },
        });
        this.cdr.detectChanges();
      },
    });
  }

  startEdit(copy: SchoolItemCopy) {
    this.editingCopyId = copy.id!;
    this.editedCopy = { 
      itemId: copy.itemId,
      barcode: copy.barcode,
      condition: copy.condition,
      copy_status: copy.copy_status,
      featured: copy.featured,
      schoolId: copy.schoolId,
      campusId: copy.campus ? copy.campus.id : 0
     };
  }

  cancelEdit() {
    this.editingCopyId = null;
    this.editedCopy = { itemId: 0, condition: "", barcode: '', copy_status: "", featured: false, schoolId: 0, campusId: 0 };
  }

  saveEdit() {
    if (!this.book) return;

    this.api.updateCopy(this.editedCopy, this.editingCopyId!).subscribe({
      next: (updatedCopy) => {
        const index = this.copies.findIndex((c) => c.id === this.editingCopyId);
        if (index > -1) {
          this.copies[index] = updatedCopy;
        }
        this.editingCopyId = null;
        this.editedCopy = { itemId: 0, condition: "", barcode: '', copy_status: "", featured: false, schoolId: 0, campusId: 0 };
        this.cdr.detectChanges();
      },
    });
  }

  toggleFeatured() {
    if (!this.book) return;
    const bookToBeFeatured = this.copies[0];
    bookToBeFeatured.featured = !bookToBeFeatured.featured

    const payload: CreateSchoolItemCopyPayload = {
      itemId: bookToBeFeatured.itemId,
      barcode: bookToBeFeatured.barcode,
      condition: bookToBeFeatured.condition,
      copy_status: bookToBeFeatured.copy_status,
      featured: bookToBeFeatured.featured,
      schoolId: bookToBeFeatured.schoolId,
      campusId: bookToBeFeatured.campus ? bookToBeFeatured.campus.id : 0
    }

    this.api.updateCopy(payload, bookToBeFeatured.id!).subscribe({
      next: () => location.reload()
    })
  }
}