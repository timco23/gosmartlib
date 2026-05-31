import { ChangeDetectorRef, Component, OnInit, inject, Renderer2, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import {
  Book,
  CampusAvailability,
  Role,
  LoginResponse,
  SchoolItemCopy,
  School,
  CreateSchoolItemCopyPayload,
  Tag,
  Review,
  CreateReview,
} from '../../../types/types';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { SchoolService } from '../../services/school.service';
import { TagService } from '../../services/tag.service';
import { GenreService } from '../../services/genre.service';
import { SubgenreService } from '../../services/subgenre.service';
import { DidacticCategoryService } from '../../services/didactic-category.service';
import { ReadingLevelService } from '../../services/reading-level.service';
import { ReviewService } from '../../services/review.service';
import { ReviewList } from '../review-list/review-list';
import { ReviewForm } from '../review-form/review-form';
import { ReadingListService } from '../../services/readingList-service';
import { CollectionService } from '../../services/collection-service';
import { PopUpComponent } from '../../widgets/pop-up/pop-up.component';
import { StarDisplayComponent } from '../../widgets/star-display/star-display.component';
import { RelatedBooks } from '../../related-books/related-books';
import { interval, Subscription, switchMap } from 'rxjs';

@Component({
  selector: 'app-book-detail-page',
  imports: [
    RouterLink,
    CommonModule,
    ReviewForm,
    ReviewList,
    PopUpComponent,
    StarDisplayComponent,
    RelatedBooks,
  ],
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
  reviewService = inject(ReviewService);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  renderer = inject(Renderer2);
  copies: SchoolItemCopy[] = [];
  campusAvailabilities: CampusAvailability[] = [];
  role = Role;
  school: School | null = null;
  isFeatured: boolean = false;
  deleteError: boolean = false;
  showDeleteConfirmation = signal(false);
  showUnableToDelete = signal(false);
  showDeleteConfirmationForCollections = signal(false);
  allTags: Tag[] = [];
  allGenres: any[] = [];
  allSubgenres: any[] = [];
  allDidacticCategories: any[] = [];
  readingLevels: any[] = [];
  bookHasCopyError: boolean = false;
  reviews: Review[] = [];
  showReviewForm: boolean = false;
  reviewServerError = '';
  isUpdating: boolean = false;
  showUpdatingWarning = signal(false);
  private updatingPollSub: Subscription | null = null;

  tagService = inject(TagService);
  genreService = inject(GenreService);
  subgenreService = inject(SubgenreService);
  didacticService = inject(DidacticCategoryService);
  readingLevelService = inject(ReadingLevelService);
  readingListService = inject(ReadingListService);
  collectionService = inject(CollectionService);

  isFavorite: boolean = false;
  favorites: any[] = [];
  
  isInWishlist: boolean = false;
  wishListBooks: any[] = [];

  ratingLevels: number[] = [5, 4, 3, 2, 1];

  
  get currentUserHasReview(): boolean {
    if (!this.currentUser) return false;
    return this.reviews.some(
      (r) => r.authorEmail === this.currentUser!.email
    );
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      if (!user?.email) return;

      this.tagService.getTags().subscribe((tags) => {
        this.allTags = tags;
      });

      this.genreService.getGenres().subscribe((g) => (this.allGenres = g));
      this.subgenreService.getSubgenres().subscribe((s) => (this.allSubgenres = s));
      this.didacticService
        .getDidacticCategories()
        .subscribe((d) => (this.allDidacticCategories = d));
      this.readingLevelService.getAll().subscribe((r) => (this.readingLevels = r));

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
            this.favorites = favorites;
            this.checkIfFavorite();
            this.checkIfInWishlist();
            this.cdr.detectChanges();
          },
        });
      });
    });
  }

  private startUpdatingPoll(bookId: number): void {
    this.updatingPollSub?.unsubscribe();
    this.updatingPollSub = interval(5000)
      .pipe(switchMap(() => this.api.getBookUpdatingStatus(bookId)))
      .subscribe({
        next: (status) => {
          this.isUpdating = status;
          this.cdr.detectChanges();
        },
        error: () => {}
      });
  }

  ngOnDestroy(): void {
    this.updatingPollSub?.unsubscribe();
  }

  get displayedReviewCount(): number {
    return this.reviews?.length ?? 0;
  }

  get displayedReviewScore(): number {
    if (!this.reviews?.length) return this.book?.reviewScore ?? 0;
    const avg = this.reviews.reduce((sum, r) => sum + r.stars, 0) / this.reviews.length;
    return Math.round(avg * 10) / 10;
  }

  getReviewPercent(stars: number): number {
    const total = this.reviews?.length ?? 0;
    if (!total) return 0;
    const count = this.reviews.filter((r) => r.stars === stars).length;
    return Math.round((count / total) * 100);
  }

  get userRoleForReviews(): string {
    const raw = (this.currentUser as any)?.role;

    let roleName = '';
    if (typeof raw === 'string') {
      roleName = raw.toUpperCase();
    } else if (typeof raw === 'number') {
      roleName = String(Role[raw] ?? '').toUpperCase();
    }

    return roleName === 'BEHEERDER' ? 'BIBLIOTHEEKBEHEERDER' : roleName;
  }

  get currentUserNameForReviews(): string {
    const u = this.currentUser as any;
    return (
      [u?.firstName ?? u?.first_name, u?.name]
        .filter((v: string | undefined) => !!v && v.trim().length > 0)
        .join(' ')
        .trim() ||
      u?.name ||
      ''
    );
  }

  loadReviews(): void {
    if (!this.book) return;
    this.reviewService.getReviews(this.book.id).subscribe({
      next: (reviews) => {
        this.reviews = reviews;
        this.cdr.detectChanges();
      },
    });
  }

  submitReview(review: CreateReview): void {
    if (!this.book) return;

    this.reviewServerError = '';
    this.reviewService.createReview(this.book.id, review).subscribe({
      next: () => {
        this.showReviewForm = false;
        this.reviewServerError = '';
        this.loadReviews();
        this.api.getBookById(this.book!.id).subscribe((b) => {
          this.book = b;
          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        this.reviewServerError =
          err?.error?.message ?? 'Slechte woorden mogen niet gebruikt worden.';
        this.cdr.detectChanges();
      },
    });
  }

  onDeleteReview(reviewId: number): void {
    if (!this.book) return;
    this.reviewService.deleteReview(this.book.id, reviewId).subscribe({
      next: () => {
        this.loadReviews();
        this.api.getBookById(this.book!.id).subscribe((b) => {
          this.book = b;
          this.cdr.detectChanges();
        });
      },
    });
  }

  onDeleteAllReviews(): void {
    if (!this.book) return;
    this.reviewService.deleteAllReviews(this.book.id).subscribe({
      next: () => {
        this.loadReviews();
        this.api.getBookById(this.book!.id).subscribe((b) => {
          this.book = b;
          this.cdr.detectChanges();
        });
      },
    });
  }

  onReportReview(event: { reviewId: number; reason?: string }): void {
    if (!this.book) return;
    
    this.reviewService
      .reportReview(this.book.id, event.reviewId, { reason: event.reason })
      .subscribe({
        next: () => {
          this.loadReviews();
          this.api.getBookById(this.book!.id).subscribe((b) => {
            this.book = b;
            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.reviewServerError = 'Rapporteren mislukt. Probeer later opnieuw.';
          this.cdr.detectChanges();
        },
      });
  }

  checkIfFavorite() {
    if (this.book && this.favorites) {
      this.isFavorite = this.favorites.some((fav: any) => fav.id === this.book?.id);
    }
  }

  checkIfInWishlist() {
    if (this.book && this.favorites) {
      this.isInWishlist = this.wishListBooks.some((fav: any) => fav.id === this.book?.id);
    }
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

  addToWishlist() {
    if (!this.book || !this.currentUser?.email) return;

    if (!this.isInWishlist) {
      this.api.addToWishlist(this.book.id, this.currentUser.email).subscribe({
        next: () => {
          this.isInWishlist = true;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Probleem bij toevoegen:', err),
      });
    } else {
      this.api.removeFromWishlist(this.book.id, this.currentUser.email).subscribe({
        next: () => {
          this.isInWishlist = false;
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
        if (data?.id) {
          this.api.getBookUpdatingStatus(data.id).subscribe({
            next: (status) => {
              this.isUpdating = status;
              this.cdr.detectChanges();
            }
          });
          this.startUpdatingPoll(data.id); 
        }
        this.loadReviews();
        this.checkIfFavorite();
        this.checkIfInWishlist();
        this.cdr.detectChanges();

        if (!this.school?.id) return;

        this.api.getCopiesByBookId(data.id, this.school.id).subscribe({
          next: (copies) => {
            this.copies = copies.filter((c) => c.copy_status !== 'KWIJT');
            if (this.copies.length > 0 && this.copies[0].featured) this.isFeatured = true;
            this.cdr.detectChanges();

            const hasUnavailableCampus = this.copiesByCampus.some((e) => e.available === 0 && e.borrowed > 0);
            if (hasUnavailableCampus) {
              this.api.getEarliestAvailabilityPerCampus(data.id).subscribe({
                next: (dates) => {
                  this.campusAvailabilities = dates;
                  this.cdr.detectChanges();
                },
              });
            }
          },
        });
      },
    });
  }

  onEditBookClick(event: Event): void {
    this.api.getBookUpdatingStatus(this.book!.id).subscribe({
      next: (status) => {
        this.isUpdating = status;
        if (status) {
          event.preventDefault();
          this.showUpdatingWarning.set(true);
        }
      }
    });
  }
  
  get copiesByCampus(): { campusName: string; available: number; borrowed: number; other: number; total: number; earliestReturnDate: string | null }[] {
    const groups = new Map<string, SchoolItemCopy[]>();
    for (const copy of this.copies) {
      const key = copy.campus?.name ?? 'Huidige campus';
      if (!groups.has(key)) groups.set(key, []);
      groups.get(key)!.push(copy);
    }

    return Array.from(groups.entries()).map(([campusName, group]) => ({
      campusName,
      available: group.filter((c) => c.copy_status === 'BESCHIKBAAR').length,
      borrowed: group.filter((c) => c.copy_status === 'UITGELEEND').length,
      other: group.filter((c) => c.copy_status !== 'BESCHIKBAAR' && c.copy_status !== 'UITGELEEND').length,
      total: group.filter((c) => c.copy_status === 'BESCHIKBAAR' || c.copy_status === 'UITGELEEND').length,
      earliestReturnDate: this.campusAvailabilities.find((d) => d.campusName === campusName)?.availableFrom ?? null,
    }));
  }


  getTagName(id: number): string {
    return this.allTags.find((t) => t.id === id)?.name ?? '';
  }

  getTagNames(): string {
    return this.book?.tags.map((tag) => tag.name).join(', ') ?? '';
  }

  getGenreNames(): string {
    return (
      this.book?.genres
        ?.map((id) => this.allGenres.find((g) => g.id === id)?.name)
        .filter(Boolean)
        .join(', ') ?? ''
    );
  }

  getSubgenreNames(): string {
    return (
      this.book?.subgenres
        ?.map((id) => this.allSubgenres.find((s) => s.id === id)?.name)
        .filter(Boolean)
        .join(', ') ?? ''
    );
  }

  getDidacticCategoryNames(): string {
    return (
      this.book?.didacticCategories
        ?.map((id) => this.allDidacticCategories.find((d) => d.id === id)?.name)
        .filter(Boolean)
        .join(', ') ?? ''
    );
  }

  get readingLevelName(): string {
    return (
      (this.book?.readingLevel as any)?.name ??
      this.readingLevels.find((r) => r.id === this.book?.readingLevel)?.name ??
      ''
    );
  }

  get readingLevelDescription(): string {
    return (
      (this.book?.readingLevel as any)?.description ??
      this.readingLevels.find((r) => r.id === this.book?.readingLevel)?.description ??
      ''
    );
  }

  deleteBook(id: number) {
    this.deleteError = false;
    this.bookHasCopyError = false;
    this.collectionService.checkIfItemIsInGloballyFeaturedCollection(id).subscribe({
      next: (isFeatured) => {
        if (isFeatured) {
          this.showUnableToDelete.set(true);
        } else {
          this.readingListService.checkIfItemIsInReadingList(id).subscribe({
            next: (isInList) => {
              if (isInList) {
                this.showDeleteConfirmation.set(true);
              } else {
                this.checkIfInCollection();
              }
            },
            error: () => {
              this.deleteError = true;
              this.cdr.detectChanges();
            },
          });
        }
      },
      error: () => {
        this.deleteError = true;
        this.cdr.detectChanges();
      },
    });
  }

  checkIfInCollection() {
    this.collectionService.checkIfItemIsInCollection(this.book!.id).subscribe({
      next: (isInCollection) => {
        if (isInCollection) {
          this.showDeleteConfirmationForCollections.set(true);
          this.showDeleteConfirmation.set(false);
        } else {
          this.confirmDelete();
        }
      },
      error: () => {
        this.deleteError = true;
        this.cdr.detectChanges();
      },
    });
  }

  confirmDelete() {
    if (this.book) {
      this.executeDelete(this.book.id);
      this.showDeleteConfirmation.set(false);
      this.showDeleteConfirmationForCollections.set(false);
    }
  }

  cancelDelete() {
    this.showDeleteConfirmation.set(false);
    this.showUnableToDelete.set(false);
    this.showDeleteConfirmationForCollections.set(false);
  }

  private executeDelete(id: number) {
    this.api.deleteBook(id).subscribe({
      next: () => this.backToList(),
      error: () => {
        this.bookHasCopyError = true;
        this.cdr.detectChanges();
      },
    });
  }

  closeNotification() {
    this.bookHasCopyError = false;
    this.deleteError = false;
    this.cdr.detectChanges();
  }

  backToList() {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/books';
    this.router.navigate([returnUrl], { replaceUrl: true });
  }

  get returnUrl(): string {
    return this.route.snapshot.queryParamMap.get('returnUrl') ?? '/books';
  }

  toggleFeatured() {
    if (!this.book) return;
    const bookToBeFeatured = this.copies[0];
    bookToBeFeatured.featured = !bookToBeFeatured.featured;

    const payload: CreateSchoolItemCopyPayload = {
      itemId: bookToBeFeatured.itemId,
      condition: bookToBeFeatured.condition,
      copy_status: bookToBeFeatured.copy_status,
      featured: bookToBeFeatured.featured,
      schoolId: bookToBeFeatured.schoolId,
      campusId: bookToBeFeatured.campus ? bookToBeFeatured.campus.id : 0,
    };

    this.api.updateCopy(payload, bookToBeFeatured.id!).subscribe({
      next: () => location.reload(),
    });
  }

  onReviewCancelled(): void {
    this.reviewServerError = '';
    this.showReviewForm = false;
  }

  handleReadingListPopUp(action: string) {
    if (action == 'cancel') {
      this.cancelDelete();
    } else {
      this.checkIfInCollection();
    }
  }

  handleCollectionPopUp(action: string) {
    if (action == 'cancel') {
      this.cancelDelete();
    } else {
      this.confirmDelete();
    }
  }
}
