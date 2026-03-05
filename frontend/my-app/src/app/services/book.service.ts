import { Injectable, signal } from '@angular/core';
import { MOCK_BOOKS } from '../app';
import { MockBook } from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class BookService {
  // Uses book mockups from app.ts, replace with the corresponding HTTP/Observable calls when the backend is available
  private books = signal<MockBook[]>(MOCK_BOOKS);

  getBooks() {
    return this.books.asReadonly();
  }

  getBookById(id: number | null) {
    return this.books().find((b) => b.id === id) ?? null;
  }

  /**
   * Enable or disable a tag for the selected book.
   *
   * - tag assigned -> delete.
   * - otherwise -> add
   */
  toggleTag(bookId: number, tagId: number) {
    this.books.update((currentBooks) => {
      return currentBooks.map((book) => {
        if (book.id !== bookId) {
          return book;
        }

        const tagAlreadySelected = book.tagIds.includes(tagId);
        const newTagIds = tagAlreadySelected
          ? book.tagIds.filter((id) => id !== tagId)
          : [...book.tagIds, tagId];

        return {
          ...book,
          tagIds: newTagIds,
        };
      });
    });
  }

  isTagSelected(bookId: number, tagId: number): boolean {
    const book = this.books().find((b) => b.id === bookId);
    if (!book) {
      return false;
    }
    return book.tagIds.includes(tagId);
  }
}
