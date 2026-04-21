import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CollectionService } from '../../services/collection-service';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Book, CreateCollectionPayload, Role, School } from '../../../types/types';
import { AuthService } from '../../services/auth.service';
import { BookService } from '../../services/book-service';
import { UserService } from '../../services/user.service';
import { SchoolService } from '../../services/school.service';
import { SelectBookPopup } from '../../reading-list-pages/select-book-popup/select-book-popup';
import { Location } from '@angular/common';

@Component({
  selector: 'app-create-collection-page',
  imports: [SelectBookPopup, FormsModule, ReactiveFormsModule],
  templateUrl: './create-collection-page.html',
  styleUrl: './create-collection-page.css',
})
export class CreateCollectionPage {
  collectionService = inject(CollectionService);
  router = inject(Router);
  authService = inject(AuthService)
  userService = inject(UserService)
  bookService = inject(BookService)
  schoolService = inject(SchoolService)
  cdr = inject(ChangeDetectorRef)
  location = inject(Location)

  form: FormGroup;
  school: School | null = null;
  loadingUserError: boolean = false;
  loading: boolean = true;
  itemIds: number[] = [];
  books: Book[] = [];
  incompleteForm: boolean = false;
  collectionAddError: boolean = false;
  currentItemId: number = 0;
  showPopup: boolean = false;
  getBookError: boolean = false;
  loadingBooks: boolean = false;
  emptyCollectionError: boolean = false;
  itemInCollectionError: boolean = false;
  globalCollection: boolean = false;
  role = Role;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      titel: ["", Validators.required],
      beschrijving: ["", Validators.required],
      uitgelicht: [false],
      globaalUitgelicht: [false]
    })
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe({
      next: (user) => {
        if (user) {
          this.userService.getUser(user.email).subscribe({
            next: (userData) => {
              if (userData.schoolName) {
                this.schoolService.getByName(userData.schoolName).subscribe({
                  next: (schoolData) => {
                    this.school = schoolData;
                    this.loading = false;
                    this.cdr.detectChanges();
                  },
                  error: () => {
                    this.loadingUserError = true; this.cdr.detectChanges();
                  }
                });
              }
            },
            error: () => { this.loadingUserError = true; this.cdr.detectChanges(); }
          })
        }
      },
      error: () => { this.loadingUserError = true; this.cdr.detectChanges(); }
    });
  }

  onSubmit(): void {
    this.collectionAddError = false;
    this.incompleteForm = false;
    this.emptyCollectionError = false;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.incompleteForm = true;
      return;
    }

    if (this.itemIds.length < 1) {
      this.emptyCollectionError = true;
      return
    }

    const raw = this.form.value;
    const newCollection: CreateCollectionPayload = {
      title: raw.titel,
      description: raw.beschrijving,
      schoolId: this.school!.id!,
      itemIds: this.itemIds,
      featured: raw.uitgelicht,
      globallyFeatured: raw.globaalUitgelicht
    }

    if (this.globalCollection) {
      newCollection.schoolId = 0;
    }

    this.collectionService.addCollection(newCollection).subscribe({
      next: () => {
        if (newCollection.schoolId != 0) {
          this.router.navigate(["/collections"])
        }
        else {
          this.router.navigate(["/collections/global"])
        }
      },
      error: () => { this.collectionAddError = true; this.cdr.detectChanges(); }
    })
  }

  addBook() {
    this.itemInCollectionError = false;
    this.cdr.detectChanges();
    if (this.itemIds.includes(this.currentItemId)) {
      this.itemInCollectionError = true;
      this.cdr.detectChanges();
      return;
    }
    this.itemIds.push(this.currentItemId);
    this.loadBooks();
  }

  loadBooks() {
    this.getBookError = false;
    this.loadingBooks = true;
    this.cdr.detectChanges();
    this.books = [];
    if (this.itemIds.length < 1) {
      this.loadingBooks = false;
      this.cdr.detectChanges();
    }
    else {
      for (const id of this.itemIds) {
        this.bookService.getBookById(id).subscribe({
          next: (book) => {
            this.books.push(book)
            if (this.books.length == this.itemIds.length) {
              this.loadingBooks = false;
              this.cdr.detectChanges();
            }
          },
          error: () => {
            this.getBookError = true;
            this.cdr.detectChanges();
          }
        })
      }
    }
  }

  showSelectionPopup() {
    this.currentItemId = 0;
    this.showPopup = true;
  }

  removeBook(id: number) {
    const index = this.itemIds.indexOf(id)
    this.itemIds.splice(index, 1)
    this.loadBooks();
  }

  goToBookDetails(id: number) {
    this.router.navigate(["/books", id])
  }

  goToCollections() {
    this.location.back();
  }

  resetFeatured() {
    this.globalCollection = !this.globalCollection;
    this.form.controls["uitgelicht"].reset()
    this.form.controls["globaalUitgelicht"].reset()
  }

  coverSrc(book: any): string | null {
    const id = Number(book?.id);
    return Number.isFinite(id) && id > 0 ? this.bookService.getBookCoverUrl(id) : null;
  }
}
