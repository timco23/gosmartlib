import { ChangeDetectorRef, Component, HostListener, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BookService } from '../../services/book-service';
import {
  Book,
  Tag,
  UpdateBookPayload,
  Genre,
  Subgenre,
  Readinglevel,
  DidacticCategory,
  SchoolItemCopy,
  School,
  CreateSchoolItemCopyPayload,
  CampusCopyGroup,
} from '../../../types/types';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { TagService } from '../../services/tag.service';
import { GenreService } from '../../services/genre.service';
import { SubgenreService } from '../../services/subgenre.service';
import { ReadingLevelService } from '../../services/reading-level.service';
import { DidacticCategoryService } from '../../services/didactic-category.service';
import { SchoolService } from '../../services/school.service';
import { CampusCopyGroupComponent } from '../../campus-copy-group/campus-copy-group.component';
import { PopUpComponent } from '../../widgets/pop-up/pop-up.component';
import { DidacticCategorySelectionMenuComponent } from '../../widgets/selection-menu-pages/didactic-category-selection-menu/didactic-category-selection-menu.component';
import { GenreSelectionMenuComponent } from '../../widgets/selection-menu-pages/genre-selection-menu/genre-selection-menu.component';
import { ReadingLevelSelectionMenuComponent } from '../../widgets/selection-menu-pages/reading-level/reading-level.component';
import { SubgenreSelectionMenuComponent } from '../../widgets/selection-menu-pages/subgenre-selection-menu/subgenre-selection-menu.component';
import { TagSelectionMenu } from '../../widgets/selection-menu-pages/tag-selection-menu/tag-selection-menu';

@Component({
  selector: 'app-update-book-page',
  imports: [
    ReactiveFormsModule,
    FormsModule,
    TagSelectionMenu,
    GenreSelectionMenuComponent,
    SubgenreSelectionMenuComponent,
    ReadingLevelSelectionMenuComponent,
    DidacticCategorySelectionMenuComponent,
    CampusCopyGroupComponent,
    PopUpComponent,
  ],
  templateUrl: './update-book-page.html',
  styleUrl: './update-book-page.css',
})
export class UpdateBookPage implements OnInit {
  api = inject(BookService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  authService = inject(AuthService);
  userService = inject(UserService);
  schoolService = inject(SchoolService);
  tagService = inject(TagService);
  genreService = inject(GenreService);
  subgenreService = inject(SubgenreService);
  readingLevelService = inject(ReadingLevelService);
  didacticCategoryService = inject(DidacticCategoryService);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  // Delete confirmation state
  DeleteConfirmation: boolean = false;

  // Lost / found confirmation state
  LostConfirmation: boolean = false;
  FoundConfirmation: boolean = false;

  // Book form state
  form: FormGroup;
  book: Book | null = null;
  error: boolean = false;
  errorMessage: string = '';
  isDidactic: boolean = false;
  previewUrl: string = '';
  incompleteForm: boolean = false;

  // Lookup data
  allTags: Tag[] = [];
  allGenres: Genre[] = [];
  allSubgenres: Subgenre[] = [];
  allReadingLevels: Readinglevel[] = [];
  allDidacticCategories: DidacticCategory[] = [];

  // Selected values
  selectedTags: Tag[] = [];
  selectedGenres: Genre[] = [];
  selectedSubgenres: Subgenre[] = [];
  selectedReadingLevel: Readinglevel | null = null;
  selectedDidacticCategories: DidacticCategory[] = [];

  // Copy list state
  copies: SchoolItemCopy[] = [];
  school: School | null = null;
  isFeatured: boolean = false;
  copyToBeDeleted: number | null = null;
  copyToBeMarkedLost: number | null = null;
  copyToBeMarkedFound: number | null = null;

  // Add copy state
  addingCopy: boolean = false;
  campusGroups: CampusCopyGroup[] = [];
  copyError: boolean = false;

  // Drag & drop state
  draggedCopyId: number | null = null;
  dropTargetCampusId: number | null = null;

  // Edit copy state
  private static readonly EMPTY_COPY_PAYLOAD: CreateSchoolItemCopyPayload = {
    itemId: 0,
    condition: '',
    copy_status: '',
    featured: false,
    schoolId: 0,
    campusId: 0,
  };
  editingCopyId: number | null = null;
  editedCopy: CreateSchoolItemCopyPayload = { ...UpdateBookPage.EMPTY_COPY_PAYLOAD };
  deleteError: string | null = null;

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
      isbn: [''],
      isDidactic: [false],
    });
  }

  ngOnInit(): void {
    this.loadSelectionOptions();
    this.loadBook();
    this.loadSchool();
    this.api.setBookUpdating(this.book!.id, "true").subscribe();
  }

  ngOnDestroy(): void {
    if (this.book?.id) {
      this.api.setBookUpdating(this.book.id, "false").subscribe();
    }
  }

  @HostListener('window:beforeunload')
  onBeforeUnload(): void {
    if (this.book?.id) {
      const url = `/api/books/${this.book.id}/updating`;
      navigator.sendBeacon(url, JSON.stringify({ updating: "false" }));
    }
  }

  loadBook() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;

    this.api.getBookById(parseInt(id)).subscribe({
      next: (data) => {
        this.book = data;
        this.tryFillForm();
        this.loadCopies();
      },
      error: (err) => {
        this.errorMessage = err;
        this.error = true;
      },
    });
  }

  loadSelectionOptions() {
    this.tagService.getTags().subscribe((tags) => {
      this.allTags = tags;
      this.tryFillForm();
    });

    this.genreService.getGenres().subscribe((genres) => {
      this.allGenres = genres;
      this.tryFillForm();
    });

    this.subgenreService.getSubgenres().subscribe((subs) => {
      this.allSubgenres = subs;
      this.tryFillForm();
    });

    this.readingLevelService.getAll().subscribe((levels) => {
      this.allReadingLevels = levels;
      this.tryFillForm();
    });

    this.didacticCategoryService.getDidacticCategories().subscribe((cats) => {
      this.allDidacticCategories = cats;
      this.tryFillForm();
    });
  }

  loadSchool() {
    this.authService.currentUser$.subscribe((user) => {
      if (!user?.email) return;

      this.userService.getUser(user.email).subscribe((userData) => {
        if (!userData.schoolName) return;

        this.schoolService.getByName(userData.schoolName).subscribe((school) => {
          this.school = school;
          if (this.book) this.loadCopies();
        });
      });
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
    this.selectedGenres = this.allGenres.filter((genre) =>
      ((this.book!.genres as number[]) ?? []).includes(genre.id),
    );

    this.selectedSubgenres = this.allSubgenres.filter((sub) =>
      ((this.book!.subgenres as number[]) ?? []).includes(sub.id),
    );

    this.selectedTags = this.allTags.filter((tag) =>
      this.book!.tags.map((t) => t.id).includes(tag.id),
    );

    this.selectedDidacticCategories = this.allDidacticCategories.filter((cat) =>
      ((this.book!.didacticCategories as number[]) ?? []).includes(cat.id),
    );

    this.selectedReadingLevel =
      this.allReadingLevels.find((rl) => rl.id === (this.book!.readingLevel as number)) ?? null;

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
      isbn: this.book.isbn ?? '',
      isDidactic: this.book.isDidactic ?? false,
    });
  }

  // Book form
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
      genres: this.selectedGenres.map((g) => g.id),
      subgenres: this.selectedSubgenres.map((s) => s.id),
      readingLevel: this.selectedReadingLevel?.id ?? null,
      didacticCategories: this.selectedDidacticCategories.map((d) => d.id),
      isDidactic: raw.isDidactic || this.selectedDidacticCategories.length > 0,
      description: raw.description,
      isbn: raw.isbn,
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
        this.api.setBookUpdating(this.book!.id, "false").subscribe();
        this.router.navigate(['/books', data.id]);
      },
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

  backToList() {
    if (this.book?.id) {
      this.api.setBookUpdating(this.book.id, "false").subscribe();
      this.router.navigate(['/books', this.book.id]);
      return;
    }
    this.router.navigate(['/books']);
  }

  // Copy list
  loadCopies() {
    if (!this.book || !this.school?.id) return;
    this.api.getCopiesByBookId(this.book.id, this.school.id).subscribe({
      next: (copies) => {
        this.copies = copies;
        if (copies.length > 0 && copies[0].featured) this.isFeatured = true;
        this.cdr.detectChanges();
      },
    });
  }

  isCopyLost(copy: SchoolItemCopy): boolean {
    return copy.copy_status === 'KWIJT';
  }

  getGroupedCopies(): { campusId: number; campusName: string; copies: SchoolItemCopy[] }[] {
    const groups: { campusId: number; campusName: string; copies: SchoolItemCopy[] }[] = [];

    groups.push({ campusId: 0, campusName: 'Geen campus', copies: [] });

    if (this.school?.campuses) {
      for (const campus of this.school.campuses) {
        groups.push({ campusId: campus.id!, campusName: campus.name, copies: [] });
      }
    }

    for (const copy of this.copies) {
      const campusId = copy.campus ? copy.campus.id : 0;
      const group = groups.find((g) => g.campusId === campusId);
      if (group) {
        group.copies.push(copy);
      }
    }

    return groups;
  }

  // Edit copy
  startEdit(copy: SchoolItemCopy) {
    this.deleteError = null;
    this.editingCopyId = copy.id!;
    this.editedCopy = {
      itemId: copy.itemId,
      condition: copy.condition,
      copy_status: copy.copy_status,
      featured: copy.featured,
      schoolId: copy.schoolId,
      campusId: copy.campus ? copy.campus.id : 0,
    };
  }

  saveEdit() {
    this.deleteError = null;
    if (!this.book) return;

    this.api.updateCopy(this.editedCopy, this.editingCopyId!).subscribe({
      next: (updatedCopy) => {
        const index = this.copies.findIndex((c) => c.id === this.editingCopyId);
        if (index > -1) {
          this.copies[index] = updatedCopy;
        }
        this.editingCopyId = null;
        this.editedCopy = { ...UpdateBookPage.EMPTY_COPY_PAYLOAD };
        this.cdr.detectChanges();
      },
    });
  }

  cancelEdit() {
    this.editingCopyId = null;
    this.editedCopy = { ...UpdateBookPage.EMPTY_COPY_PAYLOAD };
  }

  // Add or delete copy
  showAddCopyForm() {
    this.addingCopy = true;
    this.campusGroups = [
      { campusId: 0, copyCount: 1, rows: [{ condition: 'NIEUW', copy_status: 'BESCHIKBAAR' }] },
    ];
  }

  getTotalCopyCount(): number {
    return this.campusGroups.reduce((sum, g) => sum + g.rows.length, 0);
  }

  addCopies() {
    this.deleteError = null;
    if (!this.book) return;

    const allRows = this.campusGroups.flatMap((group) =>
      group.rows.map((row) => ({ ...row, campusId: group.campusId })),
    );
    const invalid = !allRows.every((row) => row.condition && row.copy_status);
    if (invalid) {
      this.copyError = true;
      return;
    }

    const newCopiesPayload: CreateSchoolItemCopyPayload[] = allRows.map((row) => ({
      itemId: this.book!.id,
      condition: row.condition,
      copy_status: row.copy_status,
      featured: this.isFeatured,
      schoolId: this.school!.id!,
      campusId: row.campusId,
    }));

    this.api.addCopies(newCopiesPayload).subscribe({
      next: (newCopies) => {
        this.copies.push(...newCopies);
        this.addingCopy = false;
        this.campusGroups = [];
        this.copyError = false;
        this.cdr.detectChanges();
      },
    });
  }

  cancelAddCopy() {
    this.addingCopy = false;
    this.campusGroups = [];
    this.copyError = false;
  }

  // Mark as lost
  showLostConfirmation(copyId: number) {
    this.copyToBeMarkedLost = copyId;
    this.LostConfirmation = true;
    this.cdr.detectChanges();
  }

  markAsLost(value: string) {
    this.LostConfirmation = false;
    if (value !== 'confirm' || this.copyToBeMarkedLost == null) return;

    this.deleteError = null;
    this.api.setCopyStatus(this.copyToBeMarkedLost, 'KWIJT').subscribe({
      next: (updatedCopy) => {
        const index = this.copies.findIndex((c) => c.id === this.copyToBeMarkedLost);
        if (index > -1) this.copies[index] = updatedCopy;
        this.copyToBeMarkedLost = null;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.deleteError = err.error || 'Er is een fout opgetreden.';
        this.cdr.detectChanges();
      },
    });
  }

  // Mark as found
  showFoundConfirmation(copyId: number) {
    this.copyToBeMarkedFound = copyId;
    this.FoundConfirmation = true;
    this.cdr.detectChanges();
  }

  markAsFound(value: string) {
    this.FoundConfirmation = false;
    if (value !== 'confirm' || this.copyToBeMarkedFound == null) return;

    this.deleteError = null;
    this.api.setCopyStatus(this.copyToBeMarkedFound, 'BESCHIKBAAR').subscribe({
      next: (updatedCopy) => {
        const index = this.copies.findIndex((c) => c.id === this.copyToBeMarkedFound);
        if (index > -1) this.copies[index] = updatedCopy;
        this.copyToBeMarkedFound = null;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.deleteError = err.error || 'Er is een fout opgetreden.';
        this.cdr.detectChanges();
      },
    });
  }

  // Delete copy
  showDeleteConfirmation(copyId: number) {
    this.copyToBeDeleted = copyId;
    this.DeleteConfirmation = true;
    this.cdr.detectChanges();
  }

  deleteCopy(value: string) {
    this.DeleteConfirmation = false;
    if (value !== 'confirm' || this.copyToBeDeleted == null) return;

    this.deleteError = null;
    this.api.deleteCopy(this.copyToBeDeleted).subscribe({
      next: () => {
        this.loadCopies();
        this.copyToBeDeleted = null;
      },
      error: (err) => {
        this.deleteError = err.error || 'Er is een fout opgetreden bij het verwijderen.';
        this.cdr.detectChanges();
      },
    });
  }

  // Drag & drop
  onDragStart(event: DragEvent, copyId: number) {
    this.draggedCopyId = copyId;
    event.dataTransfer!.effectAllowed = 'move';
  }

  onDragOver(event: DragEvent, campusId: number) {
    event.preventDefault();
    event.dataTransfer!.dropEffect = 'move';
    this.dropTargetCampusId = campusId;
  }

  onDragLeave(event: DragEvent, campusId: number) {
    if (this.dropTargetCampusId === campusId) {
      this.dropTargetCampusId = null;
    }
  }

  onDragDrop(event: DragEvent, targetCampusId: number) {
    event.preventDefault();
    this.dropTargetCampusId = null;
    this.deleteError = null;
    if (this.draggedCopyId == null) return;

    const copy = this.copies.find((c) => c.id === this.draggedCopyId);
    if (!copy) return;

    const currentCampusId = copy.campus ? copy.campus.id : 0;
    if (currentCampusId === targetCampusId) {
      this.draggedCopyId = null;
      return;
    }

    const payload: CreateSchoolItemCopyPayload = {
      itemId: copy.itemId,
      condition: copy.condition,
      copy_status: copy.copy_status,
      featured: copy.featured,
      schoolId: copy.schoolId,
      campusId: targetCampusId,
    };

    this.api.updateCopy(payload, copy.id).subscribe({
      next: () => {
        this.loadCopies();
      },
    });

    this.draggedCopyId = null;
  }

  onDragEnd() {
    this.draggedCopyId = null;
    this.dropTargetCampusId = null;
  }
}
