import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Observable } from 'rxjs';
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
  CampusCopyGroup,
  CreateBookPayload,
  CreateSchoolItemCopyPayload,
  CsvConfirmDTO,
  CsvImportDTO,
  CsvPreviewDTO,
  School,
  Tag,
  Genre,
  Subgenre,
  Readinglevel,
} from '../../../types/types';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { SchoolService } from '../../services/school.service';
import { TagSelectionMenu } from '../../selection-menu-pages/tag-selection-menu/tag-selection-menu';
import { GenreSelectionMenuComponent } from '../../selection-menu-pages/genre-selection-menu/genre-selection-menu.component';
import { SubgenreSelectionMenuComponent } from '../../selection-menu-pages/subgenre-selection-menu/subgenre-selection-menu.component';
import { ReadingLevelSelectionMenuComponent } from '../../selection-menu-pages/reading-level/reading-level.component';
import { DidacticCategorySelectionMenuComponent } from '../../selection-menu-pages/didactic-category-selection-menu/didactic-category-selection-menu.component';
import { DidacticCategory } from '../../../types/types';
import { BookImportEnrichmentComponent } from '../book-import-enrichment/book-import-enrichment.component';

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
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  form: FormGroup;
  isReadOnly: boolean = false;
  isDidactic: boolean = false;

  error: boolean = false;
  errorMessage: string = '';
  copyAlreadyExists: boolean = false;
  incompleteForm: boolean = false;
  nothingFound: boolean = false;
  missingFields: string[] = [];

  previewErrors: string[] = [];
  previewRows: BookImportRow[] | null = null;
  showEnrichment: boolean = false;
  importResult: CsvImportDTO | null = null;
  importLoading: boolean = false;
  importLoadingMessage: string = '';

  campusLoading: boolean = true;
  userError: boolean = false;
  schoolError: boolean = false;
  userHasNoSchoolError: boolean = false;
  schoolErrorMessage: string = '';

  selectedTags: Tag[] = [];
  selectedGenres: Genre[] = [];
  selectedSubgenres: Subgenre[] = [];
  selectedReadingLevel: Readinglevel | null = null;
  selectedDidacticCategories: DidacticCategory[] = [];

  campusGroups: CampusCopyGroup[] = [
    { campusId: 0, copyCount: 1, rows: [{ condition: 'NIEUW', barcode: '' }] },
  ];
  copyError: boolean = false;

  globalBookList: Observable<Book[]> | null = null;
  school: School | null = null;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      itemId: ['0', [Validators.required]],
      titel: ['', [Validators.required, Validators.maxLength(255)]],
      auteur: ['', [Validators.required, Validators.maxLength(255)]],
      beschrijving: ['', [Validators.required, Validators.maxLength(1000)]],
      uitgeverij: ['', [Validators.maxLength(255)]],
      isbn: ['', [Validators.maxLength(20)]],
      uitgaveJaar: [0, [Validators.min(0), Validators.max(new Date().getFullYear())]],
      taal: ['', [Validators.required]],
      paginaAantal: [0, [Validators.min(0)]],
      cover: ['', [Validators.maxLength(255)]],
      reviewScore: [0, [Validators.required, Validators.min(0), Validators.max(10)]],
      reviewCount: [0, [Validators.required, Validators.min(0)]],
      isDidactic: [false, [Validators.required]],
    });
  }

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
    const invalidCopies = !allRows.every((row) => row.condition && row.barcode);
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
      isDidactic: raw.isDidactic,
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
      reviewCount: raw.reviewCount,
      campusId: firstRow.campusId,
      barcode: firstRow.barcode,
      copy_status: 'BESCHIKBAAR',
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
                  barcode: row.barcode,
                  copy_status: 'BESCHIKBAAR',
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
            uitgeverij: data.publisher,
            auteur: data.author,
            isbn: data.isbn,
            uitgaveJaar: data.releaseYear,
            taal: data.language,
            paginaAantal: data.pageCount,
            cover: data.cover,
            reviewScore: data.reviewScore,
            reviewCount: data.reviewCount,
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
        { campusId: 0, copyCount: 1, rows: [{ condition: 'NIEUW', barcode: '' }] },
      ];
      this.selectedTags = [];
      this.cdr.detectChanges();
    }
  }

  onCsvFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length || !this.school?.id) return;

    this.previewErrors = [];
    this.previewRows = null;
    this.showEnrichment = false;
    this.importResult = null;
    this.importLoading = true;
    this.importLoadingMessage = '';
    this.cdr.detectChanges();

    this.api.previewImport(input.files[0], this.school.id).subscribe({
      next: (result: CsvPreviewDTO) => {
        this.importLoading = false;
        if (result.errors.length > 0) {
          this.previewErrors = result.errors;
        } else {
          this.previewRows = result.rows;
          const needsEnrichment = result.rows.some((row) => this.rowNeedsEnrichment(row));
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
    const conditionSum =
      row.conditionNieuw + row.conditionGoed + row.conditionGebruikt + row.conditionBeschadigd;
    const missingCampus = row.campus === '' && (this.school?.campuses?.length ?? 0) > 1;
    const hasExistingIsbn = row.existingIsbn != null;
    const isbnConflict = hasExistingIsbn && row.existingIsbn !== row.isbn;
    return missingCampus || conditionSum < row.copies || isbnConflict;
  }

  onEnrichmentConfirmed(rows: BookImportRow[]): void {
    this.showEnrichment = false;
    this.confirmImport(rows);
  }

  onEnrichmentSkipped(): void {
    this.showEnrichment = false;
    this.confirmImport(this.previewRows!);
  }

  onEnrichmentCancelled(): void {
    this.showEnrichment = false;
    this.previewRows = null;
    this.previewErrors = [];
    this.importResult = null;
    this.cdr.detectChanges();
  }

  private confirmImport(rows: BookImportRow[]): void {
    if (!this.school?.id) return;

    const totalCopies = rows.reduce((sum, row) => sum + row.copies, 0);
    this.importLoading = true;
    this.importLoadingMessage = `${rows.length} ${rows.length === 1 ? 'boek' : 'boeken'} en ${totalCopies} ${totalCopies === 1 ? 'exemplaar' : 'exemplaren'} worden geïmporteerd. Dit kan een tijd duren...`;
    this.previewRows = null;
    this.cdr.detectChanges();

    const dto: CsvConfirmDTO = { schoolId: this.school.id, rows };
    this.api.confirmImport(dto).subscribe({
      next: (result: CsvImportDTO) => {
        this.importResult = result;
        this.importLoading = false;
        this.importLoadingMessage = '';
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

  backToList() {
    this.router.navigate(['/books']);
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

  addCampusGroup() {
    this.campusGroups.push({ campusId: 0, copyCount: 1, rows: [{ condition: '', barcode: '' }] });
  }

  removeCampusGroup(index: number) {
    if (this.campusGroups.length > 1) {
      this.campusGroups.splice(index, 1);
    }
  }

  onGroupCopyCountChange(group: CampusCopyGroup) {
    const count = Math.max(1, group.copyCount || 1);
    const current = group.rows.length;
    if (count > current) {
      for (let i = current; i < count; i++) {
        group.rows.push({ condition: '', barcode: '' });
      }
    } else {
      group.rows.length = count;
    }
  }

  getTotalCopyCount(): number {
    return this.campusGroups.reduce((sum, g) => sum + g.rows.length, 0);
  }

  loadDetailsViaISBN() {
    const isbn: string = this.form.controls['isbn'].value;
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
              auteur: book.authors?.join(', '),
              beschrijving: book.description,
              uitgaveJaar: book.publishedDate?.substring(0, 4),
              taal: book.language,
              paginaAantal: book.pageCount,
              cover: book.imageLinks?.thumbnail,
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
          } else {
            this.nothingFound = true;
            this.cdr.detectChanges();
          }
        },
      });
    }
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
}
