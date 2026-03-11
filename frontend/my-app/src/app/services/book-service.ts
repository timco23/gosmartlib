import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import {
  Book,
  GlobalBook,
  CreateBookPayload,
  UpdateBookPayload,
  Page,
  Tag,
  GoogleBook,
} from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class BookService {
  private readonly apiBaseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  getBooksPaginated(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'title',
    sortDirection: string = 'asc',
    search: string = '',
    languages: string[] = [],
    minPages?: number,
    maxPages?: number,
    minYear?: number,
    maxYear?: number,
    minScore?: number,
    tagIds?: number[],
  ): Observable<Page<Book>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    if (search) params = params.set('search', search);
    if (languages.length > 0) languages.forEach((l) => (params = params.append('languages', l)));
    if (minPages !== undefined) params = params.set('minPages', minPages);
    if (maxPages !== undefined) params = params.set('maxPages', maxPages);
    if (minYear !== undefined) params = params.set('minYear', minYear);
    if (maxYear !== undefined) params = params.set('maxYear', maxYear);
    if (minScore !== undefined) params = params.set('minScore', minScore);
    if (tagIds && tagIds.length > 0) tagIds.forEach((id) => (params = params.append('tagIds', id)));

    return this.http.get<Page<Book>>(`${this.apiBaseUrl}/books`, { params });
  }

  getFeaturedBooks(): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiBaseUrl}/books/featured`);
  }

  getGlobalBooks(): Observable<GlobalBook[]> {
    return this.http.get<GlobalBook[]>(`${this.apiBaseUrl}/globalBooks`);
  }

  getBookById(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.apiBaseUrl}/books/${id}`);
  }

  getBookByISBN(isbn: string): Observable<GoogleBook> {
    return this.http.get<GoogleBook>(`https://www.googleapis.com/books/v1/volumes?q=isbn:${isbn}`);
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

  loadCopies(id: number) {
    return this.http.get<any[]>(`${this.apiBaseUrl}/books/copies/${id}`);
  }

  addCopy(copy: any) {
    return this.http.post(`${this.apiBaseUrl}/books/copies`, copy);
  }

  deleteCopy(id: number) {
    return this.http.delete(`${this.apiBaseUrl}/books/copies/${id}`);
  }

  updateCopy(copy: any) {
    return this.http.put(`${this.apiBaseUrl}/books/copies/${copy.id}`, copy);
  }
}
