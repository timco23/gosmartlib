import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import {
  Book,
  CampusAvailability,
  CreateBookPayload,
  CsvImportDTO,
  CsvPreviewDTO,
  CsvConfirmDTO,
  UpdateBookPayload,
  Page,
  Tag,
  BookMetadata,
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
    schoolSpecificBooks: boolean,
    minPages?: number,
    maxPages?: number,
    minScore?: number,
    tagIds?: number[],
    genreIds?: number[],
    subgenreIds?: number[],
    didacticCategoryIds?: number[],
    didactic?: boolean,
    campusId?: number,
  ): Observable<Page<Book>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection)
      .set('schoolId', schoolId)
      .set('schoolSpecificBooks', schoolSpecificBooks);

    if (search) params = params.set('search', search);
    if (languages.length > 0) languages.forEach((l) => (params = params.append('languages', l)));
    if (minPages !== undefined) params = params.set('minPages', minPages);
    if (maxPages !== undefined) params = params.set('maxPages', maxPages);
    if (minScore !== undefined) params = params.set('minScore', minScore);

    if (tagIds && tagIds.length > 0) {
      tagIds.forEach((id) => (params = params.append('tagIds', id)));
    }
    if (genreIds && genreIds.length > 0) {
      genreIds.forEach((id) => (params = params.append('genreIds', id)));
    }
    if (subgenreIds && subgenreIds.length > 0) {
      subgenreIds.forEach((id) => (params = params.append('subgenreIds', id)));
    }
    if (didacticCategoryIds && didacticCategoryIds.length > 0) {
      didacticCategoryIds.forEach((id) => (params = params.append('didacticCategoryIds', id)));
    }

    if (didactic !== undefined) {
      params = params.set('didactic', didactic);
    }
    if (campusId !== undefined) {
      params = params.set('campusId', campusId);
    }

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

  getBookMetadata(isbn: string): Observable<BookMetadata> {
    return this.http.get<BookMetadata>(`${this.apiBaseUrl}/books/metadata`, { params: { isbn } });
  }

  getBookByTitle(bookTitle: string): Observable<Book> {
    return this.http.get<Book>(`${this.apiBaseUrl}/books/search`, { params: { title: bookTitle } });
  }

  getLatestBooks(schoolId: number): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiBaseUrl}/books/latest/${schoolId}`);
  }

  getBookCoverUrl(bookId: number): string {
    return `${this.apiBaseUrl}/books/${bookId}/cover`;
  }

  getBookCoverSrc(book: { id?: number | null } | null | undefined): string {
    const id = Number(book?.id);
    if (!Number.isFinite(id) || id <= 0) return '';
    return `${this.apiBaseUrl}/books/${id}/cover`;
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
    return this.http.get<SchoolItemCopy[]>(`${this.apiBaseUrl}/books/copies/${id}`, {
      params: { schoolId: schoolId },
    });
  }

  addCopy(copy: CreateSchoolItemCopyPayload): Observable<SchoolItemCopy> {
    return this.http.post<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies`, copy);
  }

  addCopies(copies: CreateSchoolItemCopyPayload[]): Observable<SchoolItemCopy[]> {
    return this.http.post<SchoolItemCopy[]>(`${this.apiBaseUrl}/books/copies/bulk`, copies);
  }

  deleteCopy(id: number): Observable<SchoolItemCopy> {
    return this.http.delete<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/${id}`);
  }

  updateCopy(copy: CreateSchoolItemCopyPayload, id: number): Observable<SchoolItemCopy> {
    return this.http.put<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/${id}`, copy);
  }

  getCopyByItemTitle(bookTitle: string, schoolId: number): Observable<SchoolItemCopy> {
    return this.http.get<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/search`, {
      params: { title: bookTitle, schoolId: schoolId },
    });
  }

  addToFavorites(bookId: number, email: string): Observable<any> {
    return this.http.post(`${this.apiBaseUrl}/user/favorites`, null, {
      params: { itemId: bookId, email: email },
      responseType: 'text',
    });
  }

  removeFromFavorites(bookId: number, email: string): Observable<any> {
    return this.http.delete(`${this.apiBaseUrl}/user/favorites`, {
      params: { itemId: bookId, email: email },
      responseType: 'text',
    });
  }

  addToWishlist(bookId: number, email: string): Observable<any> {
    return this.http.post(`${this.apiBaseUrl}/user/wishlist`, null, {
      params: { itemId: bookId, email: email },
      responseType: 'text',
    });
  }

  removeFromWishlist(bookId: number, email: string): Observable<any> {
    return this.http.delete(`${this.apiBaseUrl}/user/wishlist`, {
      params: { itemId: bookId, email: email },
      responseType: 'text',
    });
  }

  getByBarcode(barcode: string): Observable<SchoolItemCopy> {
    return this.http.get<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/barcode/${barcode}`);
  }

  getEarliestAvailabilityPerCampus(bookId: number): Observable<CampusAvailability[]> {
    return this.http.get<CampusAvailability[]>(`${this.apiBaseUrl}/lendings/book/${bookId}/earliest-availability`);
  }

  getCopiesByBookId(bookId: number, schoolId: number): Observable<SchoolItemCopy[]> {
    return this.http.get<SchoolItemCopy[]>(`${this.apiBaseUrl}/books/copies/${bookId}`, {
      params: { schoolId: schoolId },
    });
  }

  getCopiesByIds(copyIds: number[]): Observable<SchoolItemCopy[]> {
    let params = new HttpParams();
    copyIds.forEach((id) => (params = params.append('copyIds', id.toString())));
    return this.http.get<SchoolItemCopy[]>(`${this.apiBaseUrl}/books/copies`, { params });
  }

  getCopyById(copyId: number): Observable<SchoolItemCopy> {
    return this.http.get<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/id/${copyId}`);
  }

  setCopyStatus(copyId: number, status: string): Observable<SchoolItemCopy> {
    return this.http.put<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/${copyId}/status`, null, {
      params: { status: status },
    });
  }

  getBarcodePng(copyId: number): Observable<Blob> {
    return this.http.get(`${this.apiBaseUrl}/books/copies/${copyId}/barcode`, {
      responseType: 'blob',
    });
  }

  setCondition(copyId: number, condition: string): Observable<SchoolItemCopy> {
    return this.http.put<SchoolItemCopy>(`${this.apiBaseUrl}/books/copies/${copyId}/condition`, null, {
      params: { condition: condition },
    });
  }
}
