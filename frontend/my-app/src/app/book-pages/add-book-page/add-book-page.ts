import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Observable } from 'rxjs';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AsyncPipe, NgClass } from '@angular/common';
import { GlobalBook, CreateBookPayload, LoginResponse, School } from '../../../types/types';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { SchoolService } from '../../services/school.service';

@Component({
  selector: 'app-add-book-page',
  imports: [ReactiveFormsModule, AsyncPipe, FormsModule, NgClass],
  templateUrl: './add-book-page.html',
  styleUrl: './add-book-page.css',
})
export class AddBookPage implements OnInit {
  api = inject(BookService);
  router = inject(Router);
  authService = inject(AuthService)
  userService = inject(UserService)
  schoolService = inject(SchoolService)
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  
  form: FormGroup;
  error: boolean = false;
  isReadOnly: boolean = false;
  errorMessage: string = "";
  bookAlreadyExists: boolean = false;
  previewUrl: string = "";
  selectedOption: string = "0";
  selectedCampusOption: string = "0";
  incompleteForm: boolean = false;
  nothingFound: boolean = false;
  missingFields: string[] = [];
  campusLoading: boolean = true;
  userError: boolean = false;
  schoolError: boolean = false;
  userHasNoSchoolError: boolean = false;
  schoolErrorMessage: string = "";
  
  globalBookList: Observable<GlobalBook[]> | null = null;
  school: School | null = null;

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
      tags: ["", [Validators.required]],
      campusId: ['']
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
      schoolId: this.school!.id!,
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
      reviewCount: raw.reviewCount,
      campusId: parseInt(raw.campusId)
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
      this.form.reset();
      this.previewUrl = "";
      this.selectedOption = "0";
    }

  }

  backToList() {
    this.router.navigate(["/books"]);
  }

  ngOnInit(): void {
    this.globalBookList = this.api.getGlobalBooks();
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser?.email) {
      this.userError = true;
      this.cdr.detectChanges();
      return;
    }

    this.userService.getUser(currentUser.email).subscribe({
      next: (data) => {
        if (data.schoolName != null) {
          this.schoolService.getByName(data.schoolName).subscribe({
            next: (data) => {
              this.school = data;
              this.campusLoading = false;
              this.cdr.detectChanges();
            }, error: (err) => {
              this.schoolError = true; 
              this.campusLoading = false;
              this.schoolErrorMessage = err;
              this.cdr.detectChanges();}
          })
        }
        else {
          this.userHasNoSchoolError = true;
          this.cdr.detectChanges();
        }
      }
    })
  }
}
