import { Injectable, signal } from '@angular/core';
import { MOCK_BOOKS } from './app';
import { Book } from '../types/types';

@Injectable({
  providedIn: 'root',
})
export class BookService {
  //makes use of book mockups, replace with the corresponding HTTP/Observable calls
  private books = signal<Book[]>(MOCK_BOOKS);

  getBooks() {
    return this.books;
  }

  getBookById(id: number | null) {
    return this.books().find((b) => b.id === id) ?? null;
  }

  /**
   * Enable or disable a tag for a specific book.
   *
   * - tag exists? → delete.
   * - tag exists? → add
   */
  toggleTag(bookId: number, tagId: number) {
    this.books.update((currentBooks) => {
      return currentBooks.map((book) => {
        //modify only the selected book
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
