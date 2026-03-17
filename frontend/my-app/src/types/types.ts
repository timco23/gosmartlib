export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  name: string;
  role: string;
  email: string;
}

export interface UserProfile {
  id: number;
  name: string;
  firstName: string;
  email: string;
  role: string;
  studentNumber?: string;
  className?: string;
  schoolName?: string;
}

export interface Tag {
  id: number;
  name: string;
  default: boolean;
}

export interface School {
  id?: number;
  name: string;
  address: string;
  lendingPeriodDays: number;
  extendPeriodDays: number;
  maxExtends: number;
  maxLendsStudent: number;
  maxLendTeacher: number;
  lenderNotificationPeriod: number;
  lenderLateNotificationPeriod: number;
  librarianNotificationNotReturned: number;
  librarianRequestNotification: boolean;
  externalVisibility: boolean;
  externalLending: boolean;
  reviews: boolean;
  reviewModeration: boolean;
  campuses: Campus[];
}

export interface Campus {
  id?: number;
  name: string;
}

export interface GoogleBook {
  totalItems: number;
  items?: {
    volumeInfo: {
      title?: string;
      authors?: string[];
      publisher?: string;
      publishedDate?: string;
      description?: string;
      pageCount?: number;
      imageLinks?: {
        thumbnail?: string;
      };
      language?: string;
    };
  }[];
}

export interface Book {
  id: number;
  schoolId: number;
  tags: Tag[];
  description: string;
  featured: boolean;
  itemId: number;
  title: string;
  publisher?: string;
  author: string;
  isbn?: string;
  releaseYear?: number;
  language: string;
  pageCount?: number;
  cover?: string;
  reviewScore: number;
  reviewCount: number;
  campusName: string;
  campusId: number;
}

export interface UpdateBookPayload {
  tags: number[];
  description: string;
  featured: boolean;
  title: string;
  publisher: string;
  author: string;
  releaseYear: number;
  language: string;
  pageCount: number;
  cover: string;
  campusId: number;
}

export interface CreateBookPayload {
  schoolId: number;
  tags: number[];
  description: string;
  featured: boolean;
  itemId: number;
  title: string;
  publisher: string;
  author: string;
  isbn?: string;
  releaseYear: number;
  language: string;
  pageCount: number;
  cover?: string;
  reviewScore: number;
  reviewCount: number;
  campusId: number;
}

export interface GlobalBook {
  id: number;
  title: string;
  publisher: string;
  author: string;
  isbn?: string;
  releaseYear: number;
  language: string;
  pageCount: number;
  cover?: string;
  reviewScore: number;
  reviewCount: number;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface SchoolItemCopy {
  id?: number;
  schoolItemId: number;
  condition: string;
  barcode: string;
  copy_status: string;
}

export interface CarouselSlot {
  book: Book;
  index: number;
  position: 'left-2' | 'left-1' | 'center' | 'right-1' | 'right-2';
}

export enum Role {
  LEERLING,
  LEERKRACHT,
  BEHEERDER,
  ADMIN
}