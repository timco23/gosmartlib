import { Component, inject, OnInit } from '@angular/core';
import { ReadingListService } from '../../services/readingList-service';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Book, CreateReadingListPayload, UserProfile } from '../../../types/types';
import { SelectBookPopup } from '../select-book-popup/select-book-popup';
import { BookService } from '../../services/book-service';

@Component({
  selector: 'app-create-reading-list-page',
  imports: [FormsModule, SelectBookPopup, ReactiveFormsModule],
  templateUrl: './create-reading-list-page.html',
  styleUrl: './create-reading-list-page.css',
})
export class CreateReadingListPage implements OnInit {
  readingListService = inject(ReadingListService);
  router = inject(Router);
  authService = inject(AuthService)
  userService = inject(UserService)
  bookService = inject(BookService)

  form: FormGroup;
  user: UserProfile | null = null;
  loadingUserError: boolean = false;
  loading: boolean = true;
  itemIds: number[] = [];
  books: Book[] = [];
  incompleteForm: boolean = false;
  listAddError: boolean = false;
  currentItemId: number = 0;
  showPopup: boolean = false;
  getBookError: boolean = false;
  loadingBooks: boolean = false;
  emptyListError: boolean = false;

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
              this.loading = false;
            },
            error: () => { this.loadingUserError = true }
          })
        }
      },
      error: () => { this.loadingUserError = true }
    });
  }

  onSubmit(): void {
    this.listAddError = false;
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

    this.readingListService.addReadingList(newList).subscribe({
      next: () => this.router.navigate(["/lists"]),
      error: () => this.listAddError = true
    })
  }

  addBook() {
    this.itemIds.push(this.currentItemId);
    this.loadBooks();
  }

  loadBooks() {
    this.getBookError = false;
    this.loadingBooks = true;
    this.books = [];
    this.itemIds.forEach((id) => {
      this.bookService.getBookById(id).subscribe({
        next: (book) => {
          this.books.push(book)
        },
        error: () => {
          this.getBookError = true;
        }
      })
    })
    this.loadingBooks = false;
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
