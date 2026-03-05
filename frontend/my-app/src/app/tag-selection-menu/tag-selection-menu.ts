import { Component, computed, inject, signal } from '@angular/core';
import { MockBook, Tag } from '../../types/types';
import { TagService } from '../services/tag.service';
import { BookService } from '../services/book.service';
import { FormsModule } from '@angular/forms';

/**
 * Displays all available tags and lets the user toggle which tags
 * are assigned to a specific book
 *
 * When `bookId` is provided as input the book-selector dropdown is hidden
 * When no `bookId` is provided the user can pick a book from the dropdown
 */
@Component({
  selector: 'app-tag-selection-menu',
  imports: [FormsModule],
  templateUrl: './tag-selection-menu.html',
  styleUrl: './tag-selection-menu.css',
})
export class TagSelectionMenu {
  private tagService = inject(TagService);
  private bookService = inject(BookService);

  // TODO: When the books are available, add a `bookId` input property
  // so this component is accessed without a dropdown

  tags = signal<Tag[]>([]);
  filterInput = signal('');
  books = this.bookService.getBooks();
  selectedBookId = signal<number | null>(null);
  selectedBook = computed<MockBook | null>(() => this.bookService.getBookById(this.selectedBookId()));

  //case-insensitive keyword filtering
  filteredTags = computed(() => {
    const filter = this.filterInput().toLowerCase();
    return this.tags()
      .filter((tag) => tag.name.toLowerCase().includes(filter))
      .sort((a, b) => a.name.localeCompare(b.name));
  });

  // Amount of tags added to the selected book
  selectedTagCount = computed(() => {
    const bookId = this.selectedBookId();
    if (bookId === null) return 0;
    return this.tags().filter((tag) => this.bookService.isTagSelected(bookId, tag.id)).length;
  });

  ngOnInit(): void {
    this.tagService.getTags().subscribe((tags) => this.tags.set(tags));
  }

  isTagSelected(tagId: number): boolean {
    const id = this.selectedBookId();
    return id ? this.bookService.isTagSelected(id, tagId) : false;
  }

  // enable/disable a tag for a book
  toggleTag(tagId: number): void {
    const id = this.selectedBookId();
    if (id) {
      this.bookService.toggleTag(id, tagId);
    }
  }
}
