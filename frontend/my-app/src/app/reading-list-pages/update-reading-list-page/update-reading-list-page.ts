import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { SelectBookPopup } from '../select-book-popup/select-book-popup';
import { ActivatedRoute, Router } from '@angular/router';
import { UserProfile, Book, CreateReadingListPayload, ReadingList } from '../../../types/types';
import { AuthService } from '../../services/auth.service';
import { BookService } from '../../services/book-service';
import { ReadingListService } from '../../services/readingList-service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-update-reading-list-page',
  imports: [FormsModule, ReactiveFormsModule, SelectBookPopup],
  templateUrl: './update-reading-list-page.html',
  styleUrl: './update-reading-list-page.css',
})
export class UpdateReadingListPage {
  readingListService = inject(ReadingListService);
  router = inject(Router);
  authService = inject(AuthService)
  userService = inject(UserService)
  bookService = inject(BookService)
  route = inject(ActivatedRoute)
  cdr = inject(ChangeDetectorRef)

  form: FormGroup;
  user: UserProfile | null = null;
  loadingUserError: boolean = false;
  loading: boolean = true;
  itemIds: number[] = [];
  books: Book[] = [];
  incompleteForm: boolean = false;
  listEditError: boolean = false;
  currentItemId: number = 0;
  showPopup: boolean = false;
  getBookError: boolean = false;
  loadingBooks: boolean = false;
  emptyListError: boolean = false;
  readingList: ReadingList | null = null;
  loadingListError: boolean = false;
  itemInListError: boolean = false;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      titel: ["", Validators.required],
      beschrijving: ["", Validators.required]
    })
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe({
      next: (user) => {
        if (user) {
          this.userService.getUser(user.email).subscribe({
            next: (userData) => {
              this.user = userData;
              this.getReadingList();
            },
            error: () => { this.loadingUserError = true; this.cdr.detectChanges(); }
          })
        }
      },
      error: () => { this.loadingUserError = true; this.cdr.detectChanges(); }
    });
  }

  getReadingList() {
    this.route.paramMap.subscribe((p) => {
      const id = p.get('id');
      if (!id) return;

      this.readingListService.getReadingListById(parseInt(id)).subscribe({
        next: (list) => {
          this.readingList = list;
          this.fillForm();
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: () => {this.loadingListError = true; this.cdr.detectChanges();}
      })
    })
  }

  fillForm() {
    this.form.patchValue({
      titel: this.readingList!.title,
      beschrijving: this.readingList!.description
    })
    this.readingList!.items.forEach((book) => {
      this.itemIds.push(book.id)
      this.books.push(book);
    })

  }

  onSubmit(): void {
    this.listEditError = false;
    this.incompleteForm = false;
    this.emptyListError = false;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.incompleteForm = true;
      return;
    }

    if (this.itemIds.length < 1) {
      this.emptyListError = true;
      return
    }

    const raw = this.form.value;
    const newList: CreateReadingListPayload = {
      title: raw.titel,
      description: raw.beschrijving,
      userId: this.user!.id,
      itemIds: this.itemIds
    }

    this.readingListService.updateReadingList(this.readingList!.id, newList).subscribe({
      next: () => this.router.navigate(["/lists"]),
      error: () => this.listEditError = true
    })
  }

  addBook() {
    this.itemInListError = false;
    this.cdr.detectChanges();
    if (this.itemIds.includes(this.currentItemId)) {
      this.itemInListError = true;
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

  removeBook(id:number) {
    const index = this.itemIds.indexOf(id)
    this.itemIds.splice(index, 1)
    this.loadBooks();
  }

  goToBookDetails(id:number) {
    this.router.navigate(["/books", id])
  }

  goToReadingLists() {
    this.router.navigate(["/lists"])
  }
}
