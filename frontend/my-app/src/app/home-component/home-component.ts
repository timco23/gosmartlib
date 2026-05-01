import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { BookService } from '../services/book-service';
import { LoginResponse, Book, CarouselSlot, School, Tag, Collection, Genre, Subgenre } from '../../types/types';
import { SchoolService } from '../services/school.service';
import { UserService } from '../services/user.service';
import { TagService } from '../services/tag.service';
import { CollectionService } from '../services/collection-service';
import { environment } from '../../environments/environment';
import { GenreService } from '../services/genre.service';
import { SubgenreService } from '../services/subgenre.service';
import { StarDisplayComponent } from "../widgets/star-display/star-display.component";
import { BookCarousel } from '../widgets/book-carousel/book-carousel';

@Component({
  selector: 'app-home-component',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLink, StarDisplayComponent, BookCarousel],
  templateUrl: './home-component.html',
  styleUrl: './home-component.css',
})
export class HomeComponent implements OnInit {
  private authService = inject(AuthService);
  private bookService = inject(BookService);
  private cdr = inject(ChangeDetectorRef);
  private schoolService = inject(SchoolService);
  private userService = inject(UserService);
  private collectionService = inject(CollectionService)

  currentUser: LoginResponse | null = null;
  featuredBooks: Book[] = [];
  latestBooks: Book[] = [];
  globalCollections: Collection[] = [];
  collections: Collection[] = [];
  isLoading = true;
  error: string | null = null;
  school: School | null = null;
  apiBaseUrl = environment.apiUrl;

  // Track scroll indices for different sections
  // 'featured' for featuredBooks
  // 'latest' for latestBooks
  // 'gc-<id>' for global collections
  // 'c-<id>' for school collections
  scrollIndices: { [key: string]: number } = {
    featured: 0,
    latest: 0
  };

  tagService = inject(TagService);
  genreService = inject(GenreService);
  subgenreService = inject(SubgenreService);

  allTags: Tag[] = [];
  allGenres: Genre[] = [];
  allSubgenres: Subgenre[] = [];

  ngOnInit() {
    this.tagService.getTags().subscribe((tags) => {
      this.allTags = tags;
    });
    this.genreService.getGenres().subscribe(g => {this.allGenres = g; this.cdr.detectChanges();});
    this.subgenreService.getSubgenres().subscribe(s => {this.allSubgenres = s; this.cdr.detectChanges();});

    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;

      if (!user?.email) return;

      this.userService.getUser(user.email).subscribe({
        next: (data) => {
          if (data.schoolName != null) {
            this.schoolService.getByName(data.schoolName).subscribe({
              next: (schoolData) => {
                this.school = schoolData;
                this.getCollections();
                this.getLatestBooks();
                this.getBooks();
              },
            });
          }
        },
      });
    });
  }

  getBooks() {
    if (!this.school?.id) return;

    this.bookService.getFeaturedBooks(this.school.id).subscribe({
      next: (books) => {
        this.featuredBooks = books;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Kon uitgelichte boeken niet laden.';
        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  getLatestBooks() {
    if (!this.school?.id) return;

    this.bookService.getLatestBooks(this.school.id).subscribe({
      next: (books) => {
        this.latestBooks = books;
        this.cdr.detectChanges();
      },
      error: () => {
        // We don't set this.error because it's a non-critical section
        console.error('Kon nieuwste boeken niet laden.');
      },
    });
  }

  getCollections() {
    if (!this.school?.id) return;

    this.collectionService.getAllGloballyFeaturedCollections().subscribe({
      next: (globalCollections) => {
        this.globalCollections = globalCollections;
        // Initialize indices for global collections
        globalCollections.forEach(c => {
          this.scrollIndices[`gc-${c.id}`] = 0;
        });

        this.collectionService.getFeaturedCollectionsBySchoolId(this.school!.id!).subscribe({
          next: (collections) => {
            this.collections = collections;
            // Initialize indices for school collections
            collections.forEach(c => {
              this.scrollIndices[`c-${c.id}`] = 0;
            });
            this.cdr.detectChanges();
          },
          error: () => {
            this.error = 'Kon uitgelichte collecties niet laden.';
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        })
      },
      error: () => {
        this.error = 'Kon uitgelichte globale collecties niet laden.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    })
  }

  mapTagObjects(tags: Tag[] | undefined): Tag[] {
    return (tags ?? []).map((tag) => {
      // optioneel: vervang door object uit allTags voor volledige gegevens
      return this.allTags.find((t) => t.id === tag.id) ?? tag;
    }).sort((a, b) => a.name.localeCompare(b.name));
  }

  getSortedGenres(ids: number[]): Genre[] {
    return (ids || [])
      .map(id => this.allGenres.find(g => g.id === id)!)
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  getSortedSubgenres(ids: number[]): Subgenre[] {
    return (ids || [])
      .map(id => this.allSubgenres.find(s => s.id === id)!)
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  // Returns the cards that should be visible in the carousel for a given section
  getVisibleSlotsFor(sectionKey: string, books: Book[]): CarouselSlot[] {
    const currentIndex = this.scrollIndices[sectionKey] || 0;
    const n = books.length;
    if (n === 0) return [];

    // one book on each side
    if (n === 3) {
      return [
        {
          book: books[this.wrap(currentIndex - 1, n)],
          index: this.wrap(currentIndex - 1, n),
          position: 'left-1',
        },
        { book: books[currentIndex], index: currentIndex, position: 'center' },
        {
          book: books[this.wrap(currentIndex + 1, n)],
          index: this.wrap(currentIndex + 1, n),
          position: 'right-1',
        },
      ];
    }

    const slots: CarouselSlot[] = [];
    const usedIndices = new Set<number>([currentIndex]);

    // Add 2 cards on the left
    const leftCards: CarouselSlot[] = [];
    for (let step = 1; step <= 2; step++) {
      const index = this.wrap(currentIndex - step, n);
      if (usedIndices.has(index)) break;
      usedIndices.add(index);
      const position = step === 1 ? 'left-1' : 'left-2';
      leftCards.unshift({ book: books[index], index: index, position });
    }

    // Add 2 cards on the right
    const rightCards: CarouselSlot[] = [];
    for (let step = 1; step <= 2; step++) {
      const index = this.wrap(currentIndex + step, n);
      if (usedIndices.has(index)) break;
      usedIndices.add(index);
      const position = step === 1 ? 'right-1' : 'right-2';
      rightCards.push({ book: books[index], index: index, position });
    }

    const centerCard: CarouselSlot = {
      book: books[currentIndex],
      index: currentIndex,
      position: 'center',
    };
    return [...leftCards, centerCard, ...rightCards];
  }

  prevCard(sectionKey: string, length: number) {
    this.scrollIndices[sectionKey] = this.wrap((this.scrollIndices[sectionKey] || 0) - 1, length);
  }

  nextCard(sectionKey: string, length: number) {
    this.scrollIndices[sectionKey] = this.wrap((this.scrollIndices[sectionKey] || 0) + 1, length);
  }

  goToSlide(sectionKey: string, index: number) {
    this.scrollIndices[sectionKey] = index;
  }

  getScrollIndex(sectionKey: string): number {
    return this.scrollIndices[sectionKey] || 0;
  }

  // contains the index within the bounds of the books array
  private wrap(index: number, length: number): number {
    if (length === 0) return 0;
    return ((index % length) + length) % length;
  }

  coverSrc(book: any): string | null {
    const id = Number(book?.id);
    return Number.isFinite(id) && id > 0 ? this.bookService.getBookCoverUrl(id) : null;
  }
}
