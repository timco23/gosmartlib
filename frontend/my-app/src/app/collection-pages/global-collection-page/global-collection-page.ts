import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Collection, Role, Book } from '../../../types/types';
import { AuthService } from '../../services/auth.service';
import { CollectionService } from '../../services/collection-service';
import { BookService } from '../../services/book-service';
import { BookCarousel } from '../../widgets/book-carousel/book-carousel';
import { PopUpComponent } from '../../widgets/pop-up/pop-up.component';

@Component({
  selector: 'app-global-collection-page',
  imports: [BookCarousel, PopUpComponent],
  templateUrl: './global-collection-page.html',
  styleUrl: './global-collection-page.css',
})
export class GlobalCollectionPage {
  collections: (Collection & { scrollIndex: number })[] = [];
  loading: boolean = true;
  loadingError: boolean = false;
  role = Role;
  showDeleteConfirm: boolean = false;
  pendingDeleteCollectionId: number | null = null;

  collectionService = inject(CollectionService);
  authService = inject(AuthService);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);
  bookService = inject(BookService);

  ngOnInit(): void {
    sessionStorage.setItem('collectionOrigin', 'global');
    this.getCollections();
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
    this.collectionService.getCollectionsBySchoolId(0).subscribe({
      next: (collections) => {
        this.collections = collections
          .sort((c1, c2) => {
            if (c2.globallyFeatured && !c1.globallyFeatured) return 1;
            if (c1.globallyFeatured && !c2.globallyFeatured) return -1;
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

  goBackToCollections() {
    this.router.navigate(['/collections']);
  }

  coverSrc(book: any): string | null {
    const id = Number(book?.id);
    return Number.isFinite(id) && id > 0 ? this.bookService.getBookCoverUrl(id) : null;
  }
}
