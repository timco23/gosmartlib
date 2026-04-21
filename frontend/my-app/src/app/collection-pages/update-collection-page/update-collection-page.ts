import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { School, Book, Role, CreateCollectionPayload, Collection } from '../../../types/types';
import { SelectBookPopup } from '../../reading-list-pages/select-book-popup/select-book-popup';
import { AuthService } from '../../services/auth.service';
import { BookService } from '../../services/book-service';
import { CollectionService } from '../../services/collection-service';
import { SchoolService } from '../../services/school.service';
import { UserService } from '../../services/user.service';
import { Location } from '@angular/common';

@Component({
  selector: 'app-update-collection-page',
  imports: [SelectBookPopup, FormsModule, ReactiveFormsModule],
  templateUrl: './update-collection-page.html',
  styleUrl: './update-collection-page.css',
})
export class UpdateCollectionPage {
  collectionService = inject(CollectionService);
  router = inject(Router);
  authService = inject(AuthService)
  userService = inject(UserService)
  bookService = inject(BookService)
  schoolService = inject(SchoolService)
  cdr = inject(ChangeDetectorRef)
  route = inject(ActivatedRoute)
  location = inject(Location);

  form: FormGroup;
  loading: boolean = true;
  itemIds: number[] = [];
  books: Book[] = [];
  incompleteForm: boolean = false;
  collectionEditError: boolean = false;
  currentItemId: number = 0;
  showPopup: boolean = false;
  getBookError: boolean = false;
  loadingBooks: boolean = false;
  emptyCollectionError: boolean = false;
  itemInCollectionError: boolean = false;
  globalCollection: boolean = false;
  role = Role;
  collection: Collection | null = null;
  loadingCollectionError: boolean = false;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      titel: ["", Validators.required],
      beschrijving: ["", Validators.required],
      uitgelicht: [false],
      globaalUitgelicht: [false]
    })
  }

  ngOnInit(): void {
    this.getCollection();
  }

  getCollection() {
    this.route.paramMap.subscribe((p) => {
      const id = p.get('id');
      if (!id) return;

      this.collectionService.getCollectionById(parseInt(id)).subscribe({
        next: (collection) => {
          this.collection = collection;
          this.fillForm();
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: () => { this.loadingCollectionError = true; this.cdr.detectChanges(); }
      })
    })
  }

  fillForm() {
    this.form.patchValue({
      titel: this.collection!.title,
      beschrijving: this.collection!.description,
      uitgelicht: this.collection!.featured,
      globaalUitgelicht: this.collection!.globallyFeatured
    })

    this.collection!.items.forEach((book) => {
      this.itemIds.push(book.id)
      this.books.push(book);
    })

  }

  onSubmit(): void {
    this.collectionEditError = false;
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
      schoolId: this.collection!.school ? this.collection!.school.id! : 0,
      itemIds: this.itemIds,
      featured: raw.uitgelicht,
      globallyFeatured: raw.globaalUitgelicht
    }

    if (this.globalCollection) {
      newCollection.schoolId = 0;
    }

    this.collectionService.updateCollection(this.collection!.id, newCollection).subscribe({
      next: () => {
        if (newCollection.schoolId != 0) {
          this.router.navigate(["/collections"])
        }
        else {
          this.router.navigate(["/collections/global"])
        }
      },
      error: () => { this.collectionEditError = true; this.cdr.detectChanges(); }
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

  coverSrc(book: any): string | null {
    const id = Number(book?.id);
    return Number.isFinite(id) && id > 0 ? this.bookService.getBookCoverUrl(id) : null;
  }
}
