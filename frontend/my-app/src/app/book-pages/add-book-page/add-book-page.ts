import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Observable } from 'rxjs';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AsyncPipe, NgClass } from '@angular/common';
import { GlobalBook, CreateBookPayload } from '../../../types/types';

@Component({
  selector: 'app-add-book-page',
  imports: [ReactiveFormsModule, AsyncPipe, FormsModule, NgClass],
  templateUrl: './add-book-page.html',
  styleUrl: './add-book-page.css',
})
export class AddBookPage implements OnInit {
  api = inject(BookService);
  router = inject(Router);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  form: FormGroup;
  error: boolean = false;
  isReadOnly: boolean = false;
  errorMessage: string = "";
  bookAlreadyExists: boolean = false;
  previewUrl: string = "";
  selectedOption: string = "0";
  incompleteForm: boolean = false;
  nothingFound: boolean = false;
  missingFields: string[] = [];

  globalBookList: Observable<GlobalBook[]> | null = null;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      itemId: ['', [Validators.required]],
      titel: ['', [Validators.required]],
      auteur: ['', [Validators.required]],
      beschrijving: ['', [Validators.required]],
      uitgeverij: ['', [Validators.required]],
      isbn: ['', [Validators.pattern("^[0-9]*$")]],
      uitgaveJaar: [0, [Validators.required, Validators.min(0)]],
      taal: ['', [Validators.required]],
      paginaAantal: [0, [Validators.required, Validators.min(0)]],
      cover: [''],
      reviewScore: [0, [Validators.required]],
      reviewCount: [0, [Validators.required]],
      schoolId: [0, [Validators.required]],
      tags: ["", [Validators.required]]

    })
  }

  onSubmit(): void {
    this.bookAlreadyExists = false;
    this.incompleteForm = false;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.incompleteForm = true;
      return;
    }

    const raw = this.form.value;
    const newBook: CreateBookPayload = {
      schoolId: raw.schoolId,
      tags: raw.tags,
      description: raw.beschrijving,
      featured: false,
      itemId: parseInt(raw.itemId),
      title: raw.titel,
      publisher: raw.uitgeverij,
      author: raw.auteur,
      isbn: raw.isbn,
      releaseYear: raw.uitgaveJaar,
      language: raw.taal,
      pageCount: raw.paginaAantal,
      cover: raw.cover,
      reviewScore: raw.reviewScore,
      reviewCount: raw.reviewCount
    }

    this.api.getBookByTitle(newBook.title).subscribe({
      next: (data) => {
        if (data != null) {
          this.bookAlreadyExists = true;
          this.cdr.detectChanges();
        }

        if (!this.bookAlreadyExists) {
          this.bookAlreadyExists = false;
          this.api.addBook(newBook).subscribe({
            next: (data) => {
              this.router.navigate(["/books", data.id])
            }
          })
        }
      }
    })


  }

  loadBookDetails() {
    const id = parseInt(this.selectedOption)

    if (id != 0) {
      this.error = false;
      this.cdr.detectChanges();

      this.api.getGlobalBookById(id).subscribe({
        next: (data) => {
          this.form.patchValue({
            titel: data.title,
            uitgeverij: data.publisher,
            auteur: data.author,
            isbn: data.isbn,
            uitgaveJaar: data.releaseYear,
            taal: data.language,
            paginaAantal: data.pageCount,
            cover: data.cover,
            reviewScore: data.reviewScore,
            reviewCount: data.reviewCount
          })
        }
        , error: (err) => { this.error = true; this.errorMessage = err; this.cdr.detectChanges(); }
      })
      this.isReadOnly = true;
    }
    else {
      this.isReadOnly = false;
      this.form.reset();
      this.previewUrl = "";
      this.selectedOption = "0";
    }

  }

  loadDetailsViaISBN() {
    const isbn: string = this.form.controls["isbn"].value;
    this.nothingFound = false;
    this.missingFields = [];
    this.cdr.detectChanges();

    if (isbn.length > 0) {
      this.api.getBookByISBN(isbn).subscribe({
        next: (data) => {
          if (data.totalItems > 0) {
            const book = data.items![0].volumeInfo;
            const patch: any = {};

            const fetchedBook = {
              titel: book.title,
              uitgeverij: book.publisher,
              auteur: book.authors?.join(", "),
              beschrijving: book.description,
              uitgaveJaar: book.publishedDate?.substring(0, 4),
              taal: book.language,
              paginaAantal: book.pageCount,
              cover: book.imageLinks?.thumbnail
            };

            Object.entries(fetchedBook).forEach(([key, value]) => {
              if (value) {
                patch[key] = value;
              } else {
                this.missingFields.push(key);
              }
            });

            this.cdr.detectChanges();
            this.form.patchValue(patch);
          }
          else {
            this.nothingFound = true
            this.cdr.detectChanges();
          }
        }
      })
    }
  }

  backToList() {
    this.router.navigate(["/books"]);
  }

  ngOnInit(): void {
    this.globalBookList = this.api.getGlobalBooks();
  }
}
