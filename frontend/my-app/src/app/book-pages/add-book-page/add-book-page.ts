import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Observable } from 'rxjs';
import { CreateBookPayload, GlobalBook } from '../models/book-model';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-add-book-page',
  imports: [ReactiveFormsModule, AsyncPipe, FormsModule],
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

  globalBookList: Observable<GlobalBook[]> | null = null;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      itemId: ['', [Validators.required]],
      title: ['', [Validators.required]],
      author: ['', [Validators.required]],
      description: ['', [Validators.required]],
      publisher: ['', [Validators.required]],
      isbn: [''],
      releaseYear: [0, [Validators.required, Validators.min(0)]],
      language: ['', [Validators.required]],
      pageCount: [0, [Validators.required, Validators.min(0)]],
      cover: [''],
      reviewScore: [0, [Validators.required]],
      reviewCount: [0, [Validators.required]],
      schoolId: [0, [Validators.required]],
      tags: ["", [Validators.required]]

    })
  }

  onSubmit(): void {
    this.bookAlreadyExists = false;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.value;
    const newBook: CreateBookPayload = {
      schoolId: raw.schoolId,
      tags: raw.tags,
      description: raw.description,
      featured: false,
      itemId: parseInt(raw.itemId),
      title: raw.title,
      publisher: raw.publisher,
      author: raw.author,
      isbn: raw.isbn,
      releaseYear: raw.releaseYear,
      language: raw.language,
      pageCount: raw.pageCount,
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
    const id = this.form.get('itemId')?.value

    if (id != 0) {
      this.error = false;
      this.cdr.detectChanges();

      this.api.getGlobalBookById(id).subscribe({
        next: (data) => {
          this.form.patchValue({
            title: data.title,
            publisher: data.publisher,
            author: data.author,
            isbn: data.isbn,
            releaseYear: data.releaseYear,
            language: data.language,
            pageCount: data.pageCount,
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
    }

  }

  backToList() {
    this.router.navigate(["/books"]);
  }

  ngOnInit(): void {
    this.globalBookList = this.api.getGlobalBooks();
  }
}
