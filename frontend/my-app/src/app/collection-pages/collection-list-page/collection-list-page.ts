import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Book, Collection, Role, School } from '../../../types/types';
import { CollectionService } from '../../services/collection-service';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { SchoolService } from '../../services/school.service';
import { BookService } from '../../services/book-service';
import { PopUpComponent } from '../../widgets/pop-up/pop-up.component';
import { BookCarousel } from '../../widgets/book-carousel/book-carousel';

@Component({
  selector: 'app-collection-list-page',
  imports: [PopUpComponent, BookCarousel],
  templateUrl: './collection-list-page.html',
  styleUrl: './collection-list-page.css',
})
export class CollectionListPage {
  collections: (Collection & { scrollIndex: number })[] = [];
  loading: boolean = true;
  loadingError: boolean = false;
  school: School | null = null;
  role = Role;
  showDeleteConfirm: boolean = false;
  pendingDeleteCollectionId: number | null = null;

  collectionService = inject(CollectionService);
  userService = inject(UserService);
  authService = inject(AuthService);
  schoolService = inject(SchoolService);
  bookService = inject(BookService);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {
    sessionStorage.setItem('collectionOrigin', 'local');
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser?.email) {
      this.loadingError = true;
      this.cdr.detectChanges();
      return;
    }

    this.userService.getUser(currentUser.email).subscribe({
      next: (data) => {
        if (data.schoolName != null) {
          this.schoolService.getByName(data.schoolName).subscribe({
            next: (data) => {
              this.school = data;
              this.getCollections();
            },
            error: () => {
              this.loadingError = true;
              this.cdr.detectChanges();
            },
          });
        } else {
          this.loadingError = true;
          this.cdr.detectChanges();
        }
      },
    });
  }

  deleteCollection(value: string, id: number) {
    if (value === 'confirm') {
      this.collectionService.deleteCollection(id).subscribe({
        next: () => this.getCollections(),
      });
    }
    this.showDeleteConfirm = false;
    this.pendingDeleteCollectionId = null;
    this.cdr.detectChanges();
  }

  showConfirmDelete(id: number) {
    this.pendingDeleteCollectionId = id;
    this.showDeleteConfirm = true;
    this.cdr.detectChanges();
  }

  getCollections() {
    this.loading = true;
    this.loadingError = false;

    this.collectionService.getCollectionsBySchoolId(this.school!.id!).subscribe({
      next: (collections) => {
        this.collections = collections
          .sort((c1, c2) => {
            if (c2.featured && !c1.featured) return 1;
            if (c1.featured && !c2.featured) return -1;
            return 0;
          })
          .map((collection) => ({
            ...collection,
            items: collection.items ?? [],
            scrollIndex: 0,
          }));
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingError = true;
        this.loading = false;
        this.collections = [];
        this.cdr.detectChanges();
      },
    });
  }

  editCollection(id: number) {
    this.router.navigate(['/collections', id, 'edit']);
  }

  addCollection() {
    this.router.navigate(['/collections/add']);
  }

  getVisibleBooks(collection: Collection & { scrollIndex: number }): Book[] {
    const books = collection.items || [];
    if (books.length <= 5) return books;

    const visible: Book[] = [];
    for (let i = 0; i < 5; i++) {
      const index = (collection.scrollIndex + i) % books.length;
      visible.push(books[index]);
    }
    return visible;
  }

  nextBooks(collection: Collection & { scrollIndex: number }) {
    const length = collection.items?.length ?? 0;
    if (length > 0) {
      collection.scrollIndex = (collection.scrollIndex + 1) % length;
    }
  }

  prevBooks(collection: Collection & { scrollIndex: number }) {
    const length = collection.items?.length ?? 0;
    if (length > 0) {
      collection.scrollIndex = (collection.scrollIndex - 1 + length) % length;
    }
  }

  goToBookDetails(id: number) {
    this.router.navigate(['/books', id]);
  }

  goToGlobalCollections() {
    this.router.navigate(['/collections/global']);
  }

  coverSrc(book: any): string | null {
    const id = Number(book?.id);
    return Number.isFinite(id) && id > 0 ? this.bookService.getBookCoverUrl(id) : null;
  }
}
