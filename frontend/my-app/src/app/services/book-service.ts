import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import {
  Book,
  CreateBookPayload,
  CsvImportDTO,
  CsvPreviewDTO,
  CsvConfirmDTO,
  UpdateBookPayload,
  Page,
  Tag,
  GoogleBook,
  SchoolItemCopy,
  CreateSchoolItemCopyPayload,
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
    schoolId: number,
    minPages?: number,
    maxPages?: number,
    minScore?: number,
    tagIds?: number[],
  ): Observable<Page<Book>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection)
      .set('schoolId', schoolId);

    if (search) params = params.set('search', search);
    if (languages.length > 0) languages.forEach((l) => (params = params.append('languages', l)));
    if (minPages !== undefined) params = params.set('minPages', minPages);
    if (maxPages !== undefined) params = params.set('maxPages', maxPages);
if (minScore !== undefined) params = params.set('minScore', minScore);
    if (tagIds && tagIds.length > 0) tagIds.forEach((id) => (params = params.append('tagIds', id)));

    return this.http.get<Page<Book>>(`${this.apiBaseUrl}/filteredBooks`, { params });
  }

  getFeaturedBooks(schoolId: number): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiBaseUrl}/books/featured/${schoolId}`);
  }

  getBooks(): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiBaseUrl}/books`);
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

  addBook(newBook: CreateBookPayload): Observable<Book> {
    return this.http.post<Book>(`${this.apiBaseUrl}/books`, newBook);
  }

  previewImport(file: File, schoolId: number): Observable<CsvPreviewDTO> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('schoolId', schoolId.toString());
    return this.http.post<CsvPreviewDTO>(`${this.apiBaseUrl}/books/import/preview`, formData);
  }

  confirmImport(dto: CsvConfirmDTO): Observable<CsvImportDTO> {
    return this.http.post<CsvImportDTO>(`${this.apiBaseUrl}/books/import/confirm`, dto);
  }

  updateBook(newBook: UpdateBookPayload, id: number): Observable<Book> {
    return this.http.put<Book>(`${this.apiBaseUrl}/books/${id}`, newBook);
  }

  deleteBook(id: number): Observable<Book> {
    return this.http.delete<Book>(`${this.apiBaseUrl}/books/${id}`);
  }

  loadCopies(id: number, schoolId: number): Observable<SchoolItemCopy[]> {
    return this.http.get<SchoolItemCopy[]>(`${this.apiBaseUrl}/books/copies/${id}`, { params: { schoolId: schoolId }});
  }

  addCopy(copy: CreateSchoolItemCopyPayload): Observable<SchoolItemCopy> {
    return this.http.post<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies`, copy);
  }

  deleteCopy(id: number): Observable<SchoolItemCopy> {
    return this.http.delete<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/${id}`);
  }

  updateCopy(copy: CreateSchoolItemCopyPayload, id: number): Observable<SchoolItemCopy> {
    return this.http.put<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/${id}`, copy);
  }

  getCopyByItemTitle(bookTitle: string, schoolId: number): Observable<SchoolItemCopy> {
    return this.http.get<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/search`, { params: { title: bookTitle, schoolId: schoolId } });
  }

  addToFavorites(bookId: number, email: string): Observable<any> {
    return this.http.post(`${this.apiBaseUrl}/user/favorites`, null, {
      params: { itemId: bookId, email: email },
      responseType: 'text'
    });
  }

  removeFromFavorites(bookId: number, email: string): Observable<any> {
    return this.http.delete(`${this.apiBaseUrl}/user/favorites`, {
      params: { itemId: bookId, email: email },
      responseType: 'text'
    });
  }

  getByBarcode(barcode: string): Observable<SchoolItemCopy> {
    return this.http.get<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/barcode/${barcode}`);
  }

  getCopiesByBookId(bookId: number, schoolId: number): Observable<SchoolItemCopy[]> {
    return this.http.get<SchoolItemCopy[]>(`${this.apiBaseUrl}/books/copies/${bookId}`, { params: { schoolId: schoolId } });
  }

  getCopyById(copyId: number): Observable<SchoolItemCopy> {
    return this.http.get<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/id/${copyId}`);
  }

  setCopyStatus(copyId: number, status: string): Observable<SchoolItemCopy> {
    return this.http.put<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/${copyId}/status`, null, { params: { status: status } });
  }
}