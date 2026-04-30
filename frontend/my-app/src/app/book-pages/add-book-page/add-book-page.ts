import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BookService } from '../../services/book-service';
import { forkJoin, Observable } from 'rxjs';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { AsyncPipe, NgClass } from '@angular/common';
import {
  Book,
  BookImportRow,
  BookMetadata,
  CampusCopyGroup,
  CreateBookPayload,
  CreateSchoolItemCopyPayload,
  CsvConfirmDTO,
  CsvImportDTO,
  CsvPreviewDTO,
  RowError,
  School,
  Tag,
  Genre,
  Subgenre,
  Readinglevel,
  DidacticCategory,
} from '../../../types/types';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { SchoolService } from '../../services/school.service';
import { GenreService } from '../../services/genre.service';
import { SubgenreService } from '../../services/subgenre.service';
import { ReadingLevelService } from '../../services/reading-level.service';
import { DidacticCategoryService } from '../../services/didactic-category.service';
import { BookImportEnrichmentComponent } from '../book-import-enrichment/book-import-enrichment.component';
import { CampusCopyGroupComponent } from '../../campus-copy-group/campus-copy-group.component';
import { BookSelectorPopupComponent } from '../book-selector-popup-component/book-selector-popup-component';
import { DidacticCategorySelectionMenuComponent } from '../../widgets/selection-menu-pages/didactic-category-selection-menu/didactic-category-selection-menu.component';
import { GenreSelectionMenuComponent } from '../../widgets/selection-menu-pages/genre-selection-menu/genre-selection-menu.component';
import { ReadingLevelSelectionMenuComponent } from '../../widgets/selection-menu-pages/reading-level/reading-level.component';
import { SubgenreSelectionMenuComponent } from '../../widgets/selection-menu-pages/subgenre-selection-menu/subgenre-selection-menu.component';
import { TagSelectionMenu } from '../../widgets/selection-menu-pages/tag-selection-menu/tag-selection-menu';

@Component({
  selector: 'app-add-book-page',
  imports: [
    ReactiveFormsModule,
    AsyncPipe,
    FormsModule,
    NgClass,
    TagSelectionMenu,
    GenreSelectionMenuComponent,
    SubgenreSelectionMenuComponent,
    ReadingLevelSelectionMenuComponent,
    DidacticCategorySelectionMenuComponent,
    BookImportEnrichmentComponent,
    CampusCopyGroupComponent,
    BookSelectorPopupComponent,
  ],
  templateUrl: './add-book-page.html',
  styleUrl: './add-book-page.css',
})
export class AddBookPage implements OnInit {
  api = inject(BookService);
  router = inject(Router);
  authService = inject(AuthService);
  userService = inject(UserService);
  schoolService = inject(SchoolService);
  genreService = inject(GenreService);
  subgenreService = inject(SubgenreService);
  readingLevelService = inject(ReadingLevelService);
  didacticCategoryService = inject(DidacticCategoryService);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  //Book selector
  isBookPopupOpen = false;
  selectedBookId: string | number | null = null;
  selectedBook: any | null = null;

  readonly bookId = (b: any) => b?.id ?? b?.bookId;
  readonly bookTitle = (b: any) => b?.title ?? b?.bookTitle ?? 'Onbekend boek';
  readonly bookMeta = (b: any) => [b?.author ?? b?.authorName, b?.isbn].filter(Boolean).join(' • ');

  // Book form state
  form: FormGroup;
  isReadOnly: boolean = false;
  isDidactic: boolean = false;
  error: boolean = false;
  errorMessage: string = '';
  copyAlreadyExists: boolean = false;
  incompleteForm: boolean = false;
  nothingFound: boolean = false;
  missingFields: string[] = [];

  // Selected values
  selectedTags: Tag[] = [];
  selectedGenres: Genre[] = [];
  selectedSubgenres: Subgenre[] = [];
  selectedReadingLevel: Readinglevel | null = null;
  selectedDidacticCategories: DidacticCategory[] = [];

  // Copy state
  campusGroups: CampusCopyGroup[] = [
    { campusId: 0, copyCount: 1, rows: [{ condition: 'NIEUW', copy_status: 'BESCHIKBAAR' }] },
  ];
  copyError: boolean = false;

  // School & loading state
  school: School | null = null;
  globalBookList: Observable<Book[]> | null = null;
  campusLoading: boolean = true;
  userError: boolean = false;
  schoolError: boolean = false;
  userHasNoSchoolError: boolean = false;
  schoolErrorMessage: string = '';

  // CSV import state
  previewErrors: string[] = [];
  previewRowErrors: RowError[] = [];
  previewRows: BookImportRow[] | null = null;
  showEnrichment: boolean = false;
  importResult: CsvImportDTO | null = null;
  importLoading: boolean = false;
  importLoadingMessage: string = '';

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      itemId: ['0', [Validators.required]],
      titel: ['', [Validators.required, Validators.maxLength(255)]],
      auteur: ['', [Validators.required, Validators.maxLength(255)]],
      beschrijving: ['', [Validators.maxLength(1000)]],
      uitgeverij: ['', [Validators.maxLength(255)]],
      isbn: ['', [Validators.maxLength(20)]],
      uitgaveJaar: [0, [Validators.min(0), Validators.max(new Date().getFullYear())]],
      taal: ['', [Validators.required]],
      paginaAantal: [0, [Validators.min(0)]],
      cover: ['', [Validators.maxLength(255)]],
      reviewScore: [0],
      reviewCount: [0],
      isDidactic: [false, [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.globalBookList = this.api.getBooks();
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
            },
            error: (err) => {
              this.schoolError = true;
              this.campusLoading = false;
              this.schoolErrorMessage = err;
              this.cdr.detectChanges();
            },
          });
        } else {
          this.userHasNoSchoolError = true;
          this.cdr.detectChanges();
        }
      },
    });
  }

  // Book form
  onSubmit(): void {
    this.copyAlreadyExists = false;
    this.incompleteForm = false;
    this.copyError = false;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.incompleteForm = true;
      return;
    }

    const allRows = this.campusGroups.flatMap((group) =>
      group.rows.map((row) => ({ ...row, campusId: group.campusId })),
    );
    const invalidCopies = !allRows.every((row) => row.condition && row.copy_status);
    if (invalidCopies) {
      this.copyError = true;
      return;
    }

    const raw = this.form.value;
    const firstRow = allRows[0];
    const newBook: CreateBookPayload = {
      schoolId: this.school!.id!,
      tags: this.selectedTags.map((t) => t.id),
      genres: this.selectedGenres.map((g) => g.id),
      subgenres: this.selectedSubgenres.map((s) => s.id),
      readingLevel: this.selectedReadingLevel?.id ?? null,
      didacticCategories: this.selectedDidacticCategories.map((d) => d.id),
      isDidactic: raw.isDidactic || this.selectedDidacticCategories.length > 0,
      description: raw.beschrijving?.trim() || 'Geen beschrijving beschikbaar',
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
      reviewScore: 0,
      reviewCount: 0,
      campusId: firstRow.campusId,
      copy_status: firstRow.copy_status,
      condition: firstRow.condition,
    };

    this.api.getCopyByItemTitle(newBook.title, this.school!.id!).subscribe({
      next: (data) => {
        if (data != null) {
          this.copyAlreadyExists = true;
          this.cdr.detectChanges();
        }

        if (!this.copyAlreadyExists) {
          this.copyAlreadyExists = false;
          this.api.addBook(newBook).subscribe({
            next: (createdBook) => {
              if (allRows.length > 1) {
                const extraCopies: CreateSchoolItemCopyPayload[] = allRows.slice(1).map((row) => ({
                  itemId: createdBook.id,
                  condition: row.condition,
                  copy_status: row.copy_status,
                  featured: false,
                  schoolId: this.school!.id!,
                  campusId: row.campusId,
                }));
                this.api.addCopies(extraCopies).subscribe({
                  next: () => {
                    this.router.navigate(['/books', createdBook.id]);
                  },
                });
              } else {
                this.router.navigate(['/books', createdBook.id]);
              }
            },
          });
        }
      },
    });
  }

  loadBookDetails() {
    const id = parseInt(this.form.get('itemId')?.value);

    if (id != 0) {
      this.error = false;
      this.cdr.detectChanges();

      this.api.getBookById(id).subscribe({
        next: (data) => {
          this.form.patchValue({
            titel: data.title,
            beschrijving: data.description,
            uitgeverij: data.publisher,
            auteur: data.author,
            isbn: data.isbn,
            uitgaveJaar: data.releaseYear,
            taal: data.language,
            paginaAantal: data.pageCount,
            cover: data.cover,
            reviewScore: data.reviewScore,
            reviewCount: data.reviewCount,
            isDidactic: data.isDidactic,
          });

          this.selectedTags = data.tags;

          forkJoin({
            genres: this.genreService.getGenres(),
            readingLevels: this.readingLevelService.getAll(),
            didacticCategories: this.didacticCategoryService.getDidacticCategories(),
          }).subscribe(({ genres, readingLevels, didacticCategories }) => {
            this.selectedGenres = genres.filter((g) => data.genres.includes(g.id));
            this.selectedReadingLevel =
              readingLevels.find((l) => l.id === data.readingLevel) ?? null;
            this.selectedDidacticCategories = didacticCategories.filter((d) =>
              data.didacticCategories.includes(d.id),
            );

            if (data.genres.length > 0) {
              this.subgenreService.getSubgenresByGenres(data.genres).subscribe((subgenres) => {
                this.selectedSubgenres = subgenres.filter((s) => data.subgenres.includes(s.id));
                this.cdr.detectChanges();
              });
            } else {
              this.selectedSubgenres = [];
            }

            this.cdr.detectChanges();
          });
        },
        error: (err) => {
          this.error = true;
          this.errorMessage = err;
          this.cdr.detectChanges();
        },
      });
      this.isReadOnly = true;
    } else {
      this.isReadOnly = false;
      this.form.reset({
        itemId: '0',
        titel: '',
        auteur: '',
        beschrijving: '',
        uitgeverij: '',
        isbn: '',
        uitgaveJaar: 0,
        taal: '',
        paginaAantal: 0,
        cover: '',
        reviewScore: 0,
        reviewCount: 0,
      });
      this.campusGroups = [
        { campusId: 0, copyCount: 1, rows: [{ condition: 'NIEUW', copy_status: 'BESCHIKBAAR' }] },
      ];
      this.selectedTags = [];
      this.selectedGenres = [];
      this.selectedSubgenres = [];
      this.selectedReadingLevel = null;
      this.selectedDidacticCategories = [];
      this.cdr.detectChanges();
    }
  }

  loadDetailsViaISBN() {
    const isbn: string = this.form.controls['isbn'].value;
    this.nothingFound = false;
    this.missingFields = [];
    this.cdr.detectChanges();

    if (isbn.length > 0) {
      this.api.getBookMetadata(isbn).subscribe({
        next: (data: BookMetadata) => {
          const patch: any = {};
          const fetchedBook: Record<string, unknown> = {
            titel: data.title,
            uitgeverij: data.publisher,
            auteur: data.authors,
            beschrijving: data.description,
            uitgaveJaar: data.releaseYear,
            taal: data.language,
            paginaAantal: data.pageCount,
            cover: data.coverUrl,
          };

          Object.entries(fetchedBook).forEach(([key, value]) => {
            if (value != null) {
              patch[key] = value;
            } else {
              this.missingFields.push(key);
            }
          });

          this.form.patchValue(patch);
          this.cdr.detectChanges();
        },
        error: () => {
          this.nothingFound = true;
          this.cdr.detectChanges();
        },
      });
    }
  }

  onTagsChanged(tags: Tag[]): void {
    this.selectedTags = tags;
    this.cdr.detectChanges();
  }

  onGenresChanged(genres: Genre[]): void {
    this.selectedGenres = [...genres];
    this.cdr.detectChanges();
  }

  onSubgenresChanged(subgenres: Subgenre[]): void {
    this.selectedSubgenres = subgenres;
    this.cdr.detectChanges();
  }

  onReadingLevelChanged(level: Readinglevel | null): void {
    this.selectedReadingLevel = level;
  }

  onDidacticCategoriesChanged(categories: DidacticCategory[]): void {
    this.selectedDidacticCategories = [...categories];
    this.cdr.detectChanges();
  }

  backToList() {
    this.router.navigate(['/books']);
  }

  resetForm(): void {
    this.isReadOnly = false;
    this.selectedBook = null;
    this.selectedBookId = null;
    this.nothingFound = false;
    this.missingFields = [];
    this.copyAlreadyExists = false;
    this.incompleteForm = false;
    this.copyError = false;
    this.form.reset({
      itemId: '0',
      titel: '',
      auteur: '',
      beschrijving: '',
      uitgeverij: '',
      isbn: '',
      uitgaveJaar: 0,
      taal: '',
      paginaAantal: 0,
      cover: '',
      reviewScore: 0,
      reviewCount: 0,
      isDidactic: false,
    });
    this.campusGroups = [
      { campusId: 0, copyCount: 1, rows: [{ condition: 'NIEUW', copy_status: 'BESCHIKBAAR' }] },
    ];
    this.selectedTags = [];
    this.selectedGenres = [];
    this.selectedSubgenres = [];
    this.selectedReadingLevel = null;
    this.selectedDidacticCategories = [];
    this.cdr.detectChanges();
  }

  // Copy
  addCampusGroup() {
    this.campusGroups.push({
      campusId: 0,
      copyCount: 1,
      rows: [{ condition: 'NIEUW', copy_status: 'BESCHIKBAAR' }],
    });
  }

  removeCampusGroup(index: number) {
    if (this.campusGroups.length > 1) {
      this.campusGroups.splice(index, 1);
    }
  }

  getTotalCopyCount(): number {
    return this.campusGroups.reduce((sum, g) => sum + g.rows.length, 0);
  }

  // CSV import
  onCsvFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length || !this.school?.id) return;

    this.previewErrors = [];
    this.previewRowErrors = [];
    this.previewRows = null;
    this.showEnrichment = false;
    this.importResult = null;
    this.importLoading = true;
    this.importLoadingMessage = '';
    this.cdr.detectChanges();

    this.api.previewImport(input.files[0], this.school.id).subscribe({
      next: (result: CsvPreviewDTO) => {
        this.importLoading = false;
        if (result.criticalErrors.length > 0) {
          this.previewErrors = result.criticalErrors;
        } else {
          this.previewRows = result.rows;
          this.previewRowErrors = result.errors;
          const needsEnrichment =
            result.rows.some((row) => this.rowNeedsEnrichment(row)) || result.errors.length > 0;
          if (needsEnrichment) {
            this.showEnrichment = true;
          } else {
            this.confirmImport(result.rows);
          }
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.previewErrors = ['Er is een onverwachte fout opgetreden bij het importeren.'];
        this.importLoading = false;
        this.cdr.detectChanges();
      },
    });

    input.value = '';
  }

  rowNeedsEnrichment(row: BookImportRow): boolean {
    return !!row.existingIsbn && row.existingIsbn !== row.isbn;
  }

  onEnrichmentConfirmed(rows: BookImportRow[]): void {
    this.confirmImport(rows);
  }

  onEnrichmentCancelled(): void {
    this.showEnrichment = false;
    this.previewRows = null;
    this.previewErrors = [];
    this.previewRowErrors = [];
    this.importResult = null;
    this.cdr.detectChanges();
  }

  private confirmImport(rows: BookImportRow[]): void {
    if (!this.school?.id) return;

    const totalCopies = rows.reduce(
      (sum, row) => sum + row.conditionNieuw + row.conditionGoed + row.conditionGebruikt + row.conditionBeschadigd,
      0,
    );
    this.importLoading = true;
    this.importLoadingMessage = `${rows.length} ${rows.length === 1 ? 'boek' : 'boeken'} en ${totalCopies} ${totalCopies === 1 ? 'exemplaar' : 'exemplaren'} worden geïmporteerd. Dit kan een tijd duren...`;
    this.cdr.detectChanges();

    const dto: CsvConfirmDTO = { schoolId: this.school.id, rows };
    this.api.confirmImport(dto).subscribe({
      next: (result: CsvImportDTO) => {
        this.importLoading = false;
        this.importLoadingMessage = '';
        if (result.errors.length > 0) {
          const generalErrors = result.errors.filter((e) => e.rowNumber === 0).map((e) => e.message);
          if (generalErrors.length > 0) {
            this.previewErrors = generalErrors;
          } else {
            this.previewRowErrors = result.errors;
            this.showEnrichment = true;
          }
        } else {
          this.showEnrichment = false;
          this.previewRows = null;
          this.previewRowErrors = [];
          this.importResult = result;
          this.saveToImportHistory(result);
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.previewErrors = [
          'Er is een onverwachte fout opgetreden bij het bevestigen van de import.',
        ];
        this.importLoading = false;
        this.importLoadingMessage = '';
        this.cdr.detectChanges();
      },
    });
  }

  printImportedBarcodes(): void {
    this.router.navigate(['/barcodes'], {
      queryParams: { copyIds: this.importResult!.copyIds },
    });
  }

  private saveToImportHistory(result: CsvImportDTO): void {
    const entry = {
      date: new Date().toISOString(),
      importedBooks: result.importedBooks,
      importedCopies: result.importedCopies,
      copyIds: result.copyIds,
    };
    const existing = JSON.parse(localStorage.getItem('gosmartlib_barcode_history') ?? '[]');
    existing.unshift(entry);
    localStorage.setItem('gosmartlib_barcode_history', JSON.stringify(existing.slice(0, 5)));
  }

  //Book selector methods
  openBookPopup(): void {
    this.isBookPopupOpen = true;
  }
  closeBookPopup(): void {
    this.isBookPopupOpen = false;
  }

  onBookSelected(book: any): void {
    this.selectedBook = book;
    this.selectedBookId = this.bookId(book);

    this.form.patchValue({ itemId: String(this.selectedBookId ?? '0') });
    this.loadBookDetails();

    this.closeBookPopup();
  }
}
