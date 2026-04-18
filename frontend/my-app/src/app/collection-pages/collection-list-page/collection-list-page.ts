import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Book, Collection, Role, School } from '../../../types/types';
import { CollectionService } from '../../services/collection-service';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { SchoolService } from '../../services/school.service';

@Component({
  selector: 'app-collection-list-page',
  imports: [],
  templateUrl: './collection-list-page.html',
  styleUrl: './collection-list-page.css',
})
export class CollectionListPage {
  collections: (Collection & { scrollIndex: number })[] | null = null
  loading: boolean = true;
  loadingError: boolean = false;
  school: School | null = null;
  role = Role;

  collectionService = inject(CollectionService);
  userService = inject(UserService);
  authService = inject(AuthService);
  schoolService = inject(SchoolService)
  router = inject(Router);
  cdr = inject(ChangeDetectorRef)

  ngOnInit(): void {
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

  deleteCollection(id: number) {
    this.collectionService.deleteCollection(id).subscribe({
      next: () => this.getCollections()
    });
  }

  getCollections() {
    this.loading = true;
    this.collectionService.getCollectionsBySchoolId(this.school!.id!).subscribe({
      next: (collections) => {
        this.collections = collections.sort((c1, c2) => {
          if (c2.featured && !c1.featured) {
            return 1;
          }
          else if (c1.featured && !c2.featured) {
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

  goToGlobalCollections() {
    this.router.navigate(["/collections/global"])
  }
}
