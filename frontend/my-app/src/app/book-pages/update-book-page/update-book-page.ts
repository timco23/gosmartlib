import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Book, Tag, School, UpdateBookPayload, Genre, Subgenre, Readinglevel, DidacticCategory } from '../../../types/types';
import { TagSelectionMenu } from '../../selection-menu-pages/tag-selection-menu/tag-selection-menu';
import { GenreSelectionMenuComponent } from '../../selection-menu-pages/genre-selection-menu/genre-selection-menu.component';
import { SubgenreSelectionMenuComponent } from '../../selection-menu-pages/subgenre-selection-menu/subgenre-selection-menu.component';
import { ReadingLevelSelectionMenuComponent } from '../../selection-menu-pages/reading-level/reading-level.component';
import { DidacticCategorySelectionMenuComponent } from '../../selection-menu-pages/didactic-category-selection-menu/didactic-category-selection-menu.component';
import { SchoolService } from '../../services/school.service';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { TagService } from '../../services/tag.service';
import { GenreService } from '../../services/genre.service';
import { SubgenreService } from '../../services/subgenre.service';
import { ReadingLevelService } from '../../services/reading-level.service';
import { DidacticCategoryService } from '../../services/didactic-category.service';

@Component({
  selector: 'app-update-book-page',
  imports: [
    ReactiveFormsModule,
    TagSelectionMenu,
    GenreSelectionMenuComponent,
    SubgenreSelectionMenuComponent,
    ReadingLevelSelectionMenuComponent,
    DidacticCategorySelectionMenuComponent
  ],
  templateUrl: './update-book-page.html',
  styleUrl: './update-book-page.css',
})
export class UpdateBookPage implements OnInit {
  api = inject(BookService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  schoolService = inject(SchoolService);
  tagService = inject(TagService);
  genreService = inject(GenreService);
  subgenreService = inject(SubgenreService);
  readingLevelService = inject(ReadingLevelService);
  didacticCategoryService = inject(DidacticCategoryService);
  authService = inject(AuthService)
  userService = inject(UserService)
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  previewUrl: string = '';
  incompleteForm: boolean = false;
  school: School | null = null;
  form: FormGroup;
  error: boolean = false;
  errorMessage: string = '';
  book: Book | null = null;
  isDidactic: boolean = false;

  selectedTags: Tag[] = [];
  selectedGenres: Genre[] = [];
  selectedSubgenres: Subgenre[] = [];
  selectedReadingLevel: Readinglevel | null = null;
  selectedDidacticCategories: DidacticCategory[] = [];

  allTags: Tag[] = [];
  allGenres: Genre[] = [];
  allSubgenres: Subgenre[] = [];
  allReadingLevels: Readinglevel[] = [];
  allDidacticCategories: DidacticCategory[] = [];

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      title: ['', [Validators.required]],
      author: ['', [Validators.required]],
      description: ['', [Validators.required]],
      publisher: [''],
      releaseYear: [0, [Validators.min(0)]],
      language: ['', [Validators.required]],
      pageCount: [0, [Validators.required, Validators.min(0)]],
      cover: [''],
      featured: [false, [Validators.required]],
      campusId: ['0'],
      isbn: ['', [Validators.required]],
      isDidactic: [false]
    });
  }

  onTagsChanged(tags: Tag[]): void {
    this.selectedTags = tags;
  }

  onGenresChanged(genres: Genre[]): void {
    this.selectedGenres = [...genres];
  }

  onSubgenresChanged(subgenres: Subgenre[]): void {
    this.selectedSubgenres = subgenres;
  }

  onReadingLevelChanged(level: Readinglevel | null): void {
    this.selectedReadingLevel = level;
  }

  onDidacticCategoriesChanged(categories: DidacticCategory[]): void {
    this.selectedDidacticCategories = [...categories];
  }

  onIsDidacticChanged(): void {
    this.isDidactic = this.form.get('isDidactic')?.value;
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
      tags: this.selectedTags.map(t => t.id),
      genres: this.selectedGenres.map(g => g.id),
      subgenres: this.selectedSubgenres.map(s => s.id),
      readingLevel: this.selectedReadingLevel?.id ?? null,
      didacticCategories: this.selectedDidacticCategories.map(d => d.id),
      isDidactic: raw.isDidactic,
      description: raw.description,
      isbn: raw.isbn,
      title: raw.title,
      publisher: raw.publisher,
      author: raw.author,
      releaseYear: raw.releaseYear,
      language: raw.language,
      pageCount: raw.pageCount,
      cover: raw.cover,
      campusId: raw.campusId
    };

    this.api.updateBook(newBook, this.book!.id).subscribe({
      next: (data) => {
        this.router.navigate(['/books', data.id]);
      }
    });
  }

  backToList() {
    this.router.navigate(['/books']);
  }

  ngOnInit(): void {
    this.loadLookups();
    this.loadData();
  }

  loadLookups() {
    this.tagService.getTags().subscribe(tags => {
      this.allTags = tags;
      this.tryFillForm();
    });

    this.genreService.getGenres().subscribe(genres => {
      this.allGenres = genres;
      this.tryFillForm();
    });

    this.subgenreService.getSubgenres().subscribe(subs => {
      this.allSubgenres = subs;
      this.tryFillForm();
    });

    this.readingLevelService.getAll().subscribe(levels => {
      this.allReadingLevels = levels;
      this.tryFillForm();
    });

    this.didacticCategoryService.getDidacticCategories().subscribe(cats => {
      this.allDidacticCategories = cats;
      this.tryFillForm();
    });
  }

  loadData() {
    this.route.paramMap.subscribe((p) => {
      const id = p.get('id');
      if (id) this.getBook(id);
    });
  }

  getBook(id: string) {
    this.api.getBookById(parseInt(id)).subscribe({
      next: (data) => {
        this.book = data;

        this.schoolService.getById(data.schoolId).subscribe({
          next: (school) => {
            this.school = school;
            this.tryFillForm();
          }
        });
      },
      error: (err) => {
        this.errorMessage = err;
        this.error = true;
      }
    });
  }

  tryFillForm() {
    if (
      this.book &&
      this.allTags.length &&
      this.allGenres.length &&
      this.allSubgenres.length &&
      this.allReadingLevels.length &&
      this.allDidacticCategories.length
    ) {
      this.fillForm();
      this.cdr.detectChanges();
    }
  }

  fillForm() {
    if (!this.book) return;

    this.selectedTags = this.allTags.filter(tag =>
  (this.book!.tags as number[]).includes(tag.id)
);

this.selectedGenres = this.allGenres.filter(genre =>
  (this.book!.genres as number[]).includes(genre.id)
);

this.selectedSubgenres = this.allSubgenres.filter(sub =>
  (this.book!.subgenres as number[]).includes(sub.id)
);

this.selectedReadingLevel =
  this.allReadingLevels.find(rl =>
    rl.id === (this.book!.readingLevel as number)
  ) ?? null;

this.selectedDidacticCategories = this.allDidacticCategories.filter(cat =>
  (this.book!.didacticCategories as number[]).includes(cat.id)
);

    this.isDidactic = this.book.isDidactic ?? false;

    this.form.patchValue({
      title: this.book.title,
      publisher: this.book.publisher,
      author: this.book.author,
      releaseYear: this.book.releaseYear,
      language: this.book.language,
      pageCount: this.book.pageCount,
      cover: this.book.cover,
      description: this.book.description,
      featured: this.book.featured,
      campusId: this.book.campusId,
      isDidactic: this.book.isDidactic ?? false
    });
  }
}