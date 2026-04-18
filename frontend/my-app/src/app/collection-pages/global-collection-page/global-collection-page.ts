import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Collection, School, Role, Book } from '../../../types/types';
import { AuthService } from '../../services/auth.service';
import { CollectionService } from '../../services/collection-service';
import { SchoolService } from '../../services/school.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-global-collection-page',
  imports: [],
  templateUrl: './global-collection-page.html',
  styleUrl: './global-collection-page.css',
})
export class GlobalCollectionPage {
  collections: (Collection & { scrollIndex: number })[] | null = null
  loading: boolean = true;
  loadingError: boolean = false;
  role = Role;

  collectionService = inject(CollectionService);
  authService = inject(AuthService);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef)

  ngOnInit(): void {
    this.getCollections();
  }

  deleteCollection(id: number) {
    this.collectionService.deleteCollection(id).subscribe({
      next: () => this.getCollections()
    });
  }

  getCollections() {
    this.loading = true;
    this.collectionService.getCollectionsBySchoolId(0).subscribe({
      next: (collections) => {
        this.collections = collections.sort((c1, c2) => {
          if (c2.globallyFeatured && !c1.globallyFeatured) {
            return 1;
          }
          else if (c1.globallyFeatured && !c2.globallyFeatured) {
            return -1;
          }
          return 0;
        }).map(collection => ({ ...collection, scrollIndex: 0 }));
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loadingError = true; this.cdr.detectChanges(); }
    })
  }

  editCollection(id: number) {
    this.router.navigate(['/collections', id, 'edit'])
  }

  addCollection() {
    this.router.navigate(['/collections/add'])
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
    if (collection.items.length > 0) {
      collection.scrollIndex = (collection.scrollIndex + 1) % collection.items.length;
    }
  }

  prevBooks(collection: Collection & { scrollIndex: number }) {
    if (collection.items.length > 0) {
      const length = collection.items.length;
      collection.scrollIndex = (collection.scrollIndex - 1 + length) % length;
    }
  }

  goToBookDetails(id: number) {
    this.router.navigate(['/books', id])
  }

  goBackToCollections() {
    this.router.navigate(["/collections"])
  }
}
