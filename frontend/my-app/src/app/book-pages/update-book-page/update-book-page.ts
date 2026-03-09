import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Book, Tag, UpdateBookPayload } from '../../../types/types';
import { TagSelectionMenu } from '../../tag-selection-menu/tag-selection-menu';

@Component({
  selector: 'app-update-book-page',
  imports: [ReactiveFormsModule, TagSelectionMenu],
  templateUrl: './update-book-page.html',
  styleUrl: './update-book-page.css',
})
export class UpdateBookPage implements OnInit {
  api = inject(BookService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  previewUrl: string = '';
  incompleteForm: boolean = false;

  form: FormGroup;
  error: boolean = false;
  errorMessage: string = '';

  book: Book | null = null;
  selectedTags: Tag[] = [];
  bookLoaded: boolean = false;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      title: ['', [Validators.required]],
      author: ['', [Validators.required]],
      description: ['', [Validators.required]],
      publisher: ['', [Validators.required]],
      releaseYear: [0, [Validators.required, Validators.min(0)]],
      language: ['', [Validators.required]],
      pageCount: [0, [Validators.required, Validators.min(0)]],
      cover: ['', [Validators.required]],
      featured: [false, [Validators.required]],
    });
  }

  onTagsChanged(tags: Tag[]): void {
    this.selectedTags = tags;
  }

  onSubmit(): void {
    this.incompleteForm = false;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.incompleteForm = true;
      return;
    }

    const raw = this.form.value;
    const newBook: UpdateBookPayload = {
      tags: this.selectedTags.map((t) => t.id),
      description: raw.description,
      featured: raw.featured,
      title: raw.title,
      publisher: raw.publisher,
      author: raw.author,
      releaseYear: raw.releaseYear,
      language: raw.language,
      pageCount: raw.pageCount,
      cover: raw.cover,
    };

    this.api.updateBook(newBook, this.book!.id).subscribe({
      next: (data) => {
        this.router.navigate(['/books', data.id]);
      },
    });
  }

  backToList() {
    this.router.navigate(['/books']);
  }

  getBook(id: string) {
    const data = this.api.getBookById(parseInt(id));
    data.subscribe({
      next: (data) => {
        this.book = data;
        this.fillForm();
        this.bookLoaded = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err;
        this.error = true;
        this.cdr.detectChanges();
      },
    });
  }

  loadData() {
    this.error = false;
    this.route.paramMap.subscribe((p) => {
      const id = p.get('id');
      if (id) {
        this.getBook(id);
      }
    });
  }

  fillForm() {
    if (!this.error) {
      this.selectedTags = this.book!.tags;
      this.form.patchValue({
        title: this.book!.title,
        publisher: this.book!.publisher,
        author: this.book!.author,
        releaseYear: this.book!.releaseYear,
        language: this.book!.language,
        pageCount: this.book!.pageCount,
        cover: this.book!.cover,
        description: this.book!.description,
        featured: this.book!.featured,
      });
    }
  }

  ngOnInit(): void {
    this.loadData();
  }
}
