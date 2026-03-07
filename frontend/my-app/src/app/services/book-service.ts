import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Book, GlobalBook, CreateBookPayload, UpdateBookPayload, Page } from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class BookService {
  private readonly apiBaseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  getBooksPaginated(page: number = 0, size: number = 10): Observable<Page<Book>> {
    return this.http.get<Page<Book>>(`${this.apiBaseUrl}/books`, {
      params: { page, size },
    });
  }

  getGlobalBooks(): Observable<GlobalBook[]> {
    return this.http.get<GlobalBook[]>(`${this.apiBaseUrl}/globalBooks`);
  }

  getBookById(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.apiBaseUrl}/books/${id}`);
  }

  getBookByTitle(bookTitle: string): Observable<Book> {
    return this.http.get<Book>(`${this.apiBaseUrl}/books/search`, { params: { title: bookTitle } });
  }

  getGlobalBookById(id: number): Observable<GlobalBook> {
    return this.http.get<GlobalBook>(`${this.apiBaseUrl}/globalBooks/${id}`);
  }

  addBook(newBook: CreateBookPayload): Observable<Book> {
    return this.http.post<Book>(`${this.apiBaseUrl}/books`, newBook);
  }

  updateBook(newBook: UpdateBookPayload, id: number): Observable<Book> {
    return this.http.put<Book>(`${this.apiBaseUrl}/books/${id}`, newBook);
  }

  deleteBook(id: number): Observable<Book> {
    return this.http.delete<Book>(`${this.apiBaseUrl}/books/${id}`);
  }
}
