import { ChangeDetectorRef, Component, inject } from '@angular/core';
import {
  FormsModule,
  ReactiveFormsModule,
  FormGroup,
  FormBuilder,
  Validators,
} from '@angular/forms';
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
  authService = inject(AuthService);
  userService = inject(UserService);
  bookService = inject(BookService);
  schoolService = inject(SchoolService);
  cdr = inject(ChangeDetectorRef);
  route = inject(ActivatedRoute);
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

  private origin: 'local' | 'global' | null = null;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      titel: ['', Validators.required],
      beschrijving: ['', Validators.required],
      uitgelicht: [false],
      globaalUitgelicht: [false],
    });
  }

  private getOrigin(): 'local' | 'global' | null {
    const fromQuery = this.route.snapshot.queryParamMap.get('origin');
    const fromState = (history.state?.origin as string | undefined) ?? null;
    const fromStorage = sessionStorage.getItem('collectionOrigin');
    const origin = fromQuery ?? fromState ?? fromStorage;
    return origin === 'global' || origin === 'local' ? origin : null;
  }

  ngOnInit(): void {
    this.origin = this.getOrigin();
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
        error: () => {
          this.loadingCollectionError = true;
          this.cdr.detectChanges();
        },
      });
    });
  }

  fillForm() {
    this.itemIds = [];
    this.books = [];

    if (this.origin === 'global') this.globalCollection = true;
    if (this.origin === 'local') this.globalCollection = false;

    const schoolId = (this.collection as any)?.schoolId ?? this.collection?.school?.id ?? 0;
    this.globalCollection = !schoolId || schoolId === 0;

    this.form.patchValue({
      titel: this.collection!.title,
      beschrijving: this.collection!.description,
      uitgelicht: this.collection!.featured,
      globaalUitgelicht: this.collection!.globallyFeatured,
    });

    for (const book of this.collection?.items ?? []) {
      this.itemIds.push(book.id);
      this.books.push(book);
    }
  }

  onSubmit(): void {
    this.collectionEditError = false;
    this.incompleteForm = false;
    this.emptyCollectionError = false;

    const isGlobal =
      this.origin === 'global' ? true : this.origin === 'local' ? false : this.globalCollection;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.incompleteForm = true;
      return;
    }

    if (this.itemIds.length < 1) {
      this.emptyCollectionError = true;
      return;
    }

    const raw = this.form.value;

    const schoolId = (this.collection as any)?.schoolId ?? this.collection?.school?.id ?? 0;

    const newCollection: CreateCollectionPayload = {
      title: raw.titel,
      description: raw.beschrijving,
      schoolId: isGlobal ? 0 : schoolId,
      itemIds: this.itemIds,
      featured: raw.uitgelicht,
      globallyFeatured: raw.globaalUitgelicht,
    };

    if (this.globalCollection) {
      newCollection.schoolId = 0;
    }

    this.collectionService.updateCollection(this.collection!.id, newCollection).subscribe({
      next: () => {
        this.router.navigate([isGlobal ? '/collections/global' : '/collections']);
      },
      error: () => {
        this.collectionEditError = true;
        this.cdr.detectChanges();
      },
    });
  }

  addBook() {
    this.itemInCollectionError = false;
    this.cdr.detectChanges();

    if (!this.currentItemId) return;

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
    } else {
      for (const id of this.itemIds) {
        this.bookService.getBookById(id).subscribe({
          next: (book) => {
            this.books.push(book);
            if (this.books.length == this.itemIds.length) {
              this.loadingBooks = false;
              this.cdr.detectChanges();
            }
          },
          error: () => {
            this.getBookError = true;
            this.cdr.detectChanges();
          },
        });
      }
    }
  }

  showSelectionPopup() {
    this.currentItemId = 0;
    this.showPopup = true;
  }

  removeBook(id: number) {
    this.itemIds = this.itemIds.filter((itemId) => itemId !== id);
    this.books = this.books.filter((book) => book.id !== id);
    this.loadBooks();
  }

  goToBookDetails(id: number) {
    this.router.navigate(['/books', id]);
  }

  goToCollections() {
    this.location.back();
  }

  coverSrc(book: any): string | null {
    const id = Number(book?.id);
    return Number.isFinite(id) && id > 0 ? this.bookService.getBookCoverUrl(id) : null;
  }
}
