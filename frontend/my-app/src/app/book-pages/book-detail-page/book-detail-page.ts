import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import {
  Book,
  Role,
  LoginResponse,
  SchoolItemCopy,
  School,
  CreateSchoolItemCopyPayload,
  Tag,
} from '../../../types/types';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { SchoolService } from '../../services/school.service';
import { TagService } from '../../services/tag.service';
import { GenreService } from '../../services/genre.service';
import { SubgenreService } from '../../services/subgenre.service';
import { DidacticCategoryService } from '../../services/didactic-category.service';
import { ReadingLevelService } from '../../services/reading-level.service';

@Component({
  selector: 'app-book-detail-page',
  imports: [RouterLink, FormsModule, CommonModule],
  templateUrl: './book-detail-page.html',
  styleUrl: './book-detail-page.css',
})
export class BookDetailPage implements OnInit {
  book: Book | null = null;
  currentUser: LoginResponse | null = null;
  api = inject(BookService);
  userApi = inject(UserService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  authService = inject(AuthService);
  schoolService = inject(SchoolService);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  copies: any[] = [];
  addingCopy: boolean = false;
  role = Role;
  copyError: boolean = false;
  school: School | null = null;
  isFeatured: boolean = false;

  allTags: Tag[] = [];
  allGenres: any[] = [];
  allSubgenres: any[] = [];
  allDidacticCategories: any[] = [];
  readingLevels: any[] = [];

  tagService = inject(TagService);
  genreService = inject(GenreService);
  subgenreService = inject(SubgenreService);
  didacticService = inject(DidacticCategoryService);
  readingLevelService = inject(ReadingLevelService);

  editingCopyId: number | null = null;
  editedCopy: SchoolItemCopy = {
    itemId: 0,
    condition: '',
    barcode: '',
    copy_status: '',
    featured: false,
    schoolId: 0,
  };
  isFavorite: boolean = false;

  newCopy: CreateSchoolItemCopyPayload = {
    itemId: 0,
    condition: '',
    barcode: '',
    copy_status: 'BESCHIKBAAR',
    featured: false,
    schoolId: 0,
  };

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      if (!user?.email) return;

      this.tagService.getTags().subscribe(tags => {
        this.allTags = tags;
      });

      this.genreService.getGenres().subscribe(g => this.allGenres = g);
      this.subgenreService.getSubgenres().subscribe(s => this.allSubgenres = s);
      this.didacticService.getDidacticCategories().subscribe(d => this.allDidacticCategories = d);
      this.readingLevelService.getAll().subscribe(r => this.readingLevels = r);

      this.route.paramMap.subscribe((p) => {
        const id = p.get('id');
        if (!id) return;

        this.userApi.getUser(user.email).subscribe({
          next: (data) => {
            if (data.schoolName) {
              this.schoolService.getByName(data.schoolName).subscribe({
                next: (schoolData) => {
                  this.school = schoolData;
                  this.getBook(id);
                },
              });
            } else {
              this.getBook(id);
            }
          },
        });

        this.userApi.getFavorites().subscribe({
          next: (favorites) => {
            this.isFavorite = favorites.some((fav: any) => fav.id === this.book?.id);
            this.cdr.detectChanges();
          },
        });
      });
    });
  }

  addToFavorites() {
    if (!this.book || !this.currentUser?.email) return;

    if (!this.isFavorite) {
      this.api.addToFavorites(this.book.id, this.currentUser.email).subscribe({
        next: () => {
          this.isFavorite = true;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Probleem bij toevoegen:', err),
      });
    } else {
      this.api.removeFromFavorites(this.book.id, this.currentUser.email).subscribe({
        next: () => {
          this.isFavorite = false;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Probleem bij verwijderen:', err),
      });
    }
  }

  getBook(id: string) {
    this.api.getBookById(parseInt(id)).subscribe({
      next: (data) => {
        this.book = data;
        this.cdr.detectChanges();

        if (!this.school?.id) return;

        this.api.loadCopies(data.id, this.school.id).subscribe({
          next: (copies) => {
            this.copies = copies;
            if (copies.length > 0 && copies[0].featured) this.isFeatured = true;
            this.cdr.detectChanges();
          },
        });
      },
    });
  }

  getTagName(id: number): string {
    return this.allTags.find(t => t.id === id)?.name ?? '';
  }

  getTagNames(): string {
    return this.book?.tags.map(tag => tag.name).join(', ') ?? '';
  }

  getGenreNames(): string {
    return this.book?.genres
      ?.map(id => this.allGenres.find(g => g.id === id)?.name)
      .filter(Boolean)
      .join(', ') ?? '';
  }

  getSubgenreNames(): string {
    return this.book?.subgenres
      ?.map(id => this.allSubgenres.find(s => s.id === id)?.name)
      .filter(Boolean)
      .join(', ') ?? '';
  }

  getDidacticCategoryNames(): string {
    return this.book?.didacticCategories
      ?.map(id => this.allDidacticCategories.find(d => d.id === id)?.name)
      .filter(Boolean)
      .join(', ') ?? '';
  }

  get readingLevelName(): string {
    return this.readingLevels.find(r => r.id === this.book?.readingLevel)?.name ?? '';
  }

  deleteBook(id: number) {
    this.api.deleteBook(id).subscribe({ next: () => this.backToList() });
  }

  backToList() {
    this.router.navigate(['/books']);
  }

  addCopy() {
    if (!this.book) return;
    if (this.newCopy.condition == '') {
      this.copyError = true;
      return;
    }

    this.newCopy.itemId = this.book.id;
    this.newCopy.schoolId = this.school!.id!;

    this.api.addCopy(this.newCopy).subscribe({
      next: (copy) => {
        this.copies.push(copy);

        this.newCopy = {
          itemId: this.book!.id,
          condition: '',
          barcode: '',
          copy_status: 'BESCHIKBAAR',
          featured: false,
          schoolId: 0,
        };
        this.addingCopy = false;
        this.cdr.detectChanges();
      },
    });
  }

  cancelAddCopy() {
    this.addingCopy = false;
    this.newCopy = {
      itemId: this.book!.id,
      condition: '',
      barcode: '',
      copy_status: 'BESCHIKBAAR',
      featured: false,
      schoolId: 0,
    };
  }

  showAddCopyForm() {
    this.addingCopy = true;
  }

  deleteCopy(copy: any) {
    this.api.deleteCopy(copy!).subscribe({
      next: () => {
        this.copies = [];
        this.api.loadCopies(this.book!.id, this.school!.id!).subscribe({
          next: (copies) => {
            this.copies = copies;
            if (copies.length <= 0) {
              this.backToList();
            }
            this.cdr.detectChanges();
          },
        });
        this.cdr.detectChanges();
      },
    });
  }

  startEdit(copy: SchoolItemCopy) {
    this.editingCopyId = copy.id!;
    this.editedCopy = { ...copy };
  }

  cancelEdit() {
    this.editingCopyId = null;
    this.editedCopy = {
      itemId: 0,
      condition: '',
      barcode: '',
      copy_status: '',
      featured: false,
      schoolId: 0,
    };
  }

  saveEdit() {
    if (!this.book) return;

    const payload: CreateSchoolItemCopyPayload = {
      itemId: this.editedCopy.itemId,
      barcode: this.editedCopy.barcode,
      condition: this.editedCopy.condition,
      copy_status: this.editedCopy.copy_status,
      featured: this.editedCopy.featured,
      schoolId: this.editedCopy.schoolId,
    };

    this.api.updateCopy(payload, this.editedCopy.id!).subscribe({
      next: (updatedCopy) => {
        const index = this.copies.findIndex((c) => c.id === this.editingCopyId);
        if (index > -1) {
          this.copies[index] = updatedCopy;
        }
        this.editingCopyId = null;
        this.editedCopy = {
          itemId: 0,
          condition: '',
          barcode: '',
          copy_status: '',
          featured: false,
          schoolId: 0,
        };
        this.cdr.detectChanges();
      },
    });
  }

  toggleFeatured() {
    if (!this.book) return;
    const bookToBeFeatured = this.copies[0];
    bookToBeFeatured.featured = !bookToBeFeatured.featured;

    const payload: CreateSchoolItemCopyPayload = {
      itemId: bookToBeFeatured.item.id,
      barcode: bookToBeFeatured.barcode,
      condition: bookToBeFeatured.condition,
      copy_status: bookToBeFeatured.copy_status,
      featured: bookToBeFeatured.featured,
      schoolId: bookToBeFeatured.school.id,
    };

    this.api.updateCopy(payload, bookToBeFeatured.id!).subscribe({
      next: () => location.reload(),
    });
  }
}
