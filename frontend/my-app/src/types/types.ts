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
  classes: Class[];
  schoolName?: string;
  schoolId?: number;
  items_borrowed: number;
  deletionRequestedAt?: string | null;
}

export interface Tag {
  id: number;
  name: string;
}

export interface Genre {
  id: number;
  name: string;
  subgenres?: Subgenre[];
}

export interface Subgenre {
  id: number;
  name: string;
}

export interface Readinglevel {
  id: number;
  name: string;
  description: string;
}

export interface DidacticCategory {
  id: number;
  name: string;
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

export interface BookMetadata {
  title?: string;
  authors?: string;
  description?: string;
  language?: string;
  publisher?: string;
  pageCount?: number;
  releaseYear?: number;
  coverUrl?: string;
}

export interface Book {
  id: number;
  tags: Tag[];
  genres: number[];
  subgenres: number[];
  readingLevel: number | null;
  didacticCategories: number[];
  isDidactic: boolean;
  description: string;
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
}

export interface UpdateBookPayload {
  tags: number[];
  genres: number[];
  subgenres: number[];
  readingLevel: number | null;
  didacticCategories: number[];
  isDidactic: boolean;
  description: string;
  title: string;
  publisher: string;
  author: string;
  releaseYear: number;
  language: string;
  pageCount: number;
  cover: string;
  isbn: string;
}

export interface CreateBookPayload {
  schoolId: number;
  tags: number[];
  genres: number[];
  subgenres: number[];
  readingLevel: number | null;
  didacticCategories: number[];
  isDidactic: boolean;
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
  copy_status: string;
  condition: string;
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
  id: number;
  itemId: number;
  condition: string;
  barcode: string;
  featured: boolean;
  copy_status: string;
  schoolId: number;
  campus?: {
    id: number;
    name: string;
  };
}

export interface CreateSchoolItemCopyPayload {
  itemId: number;
  condition: string;
  copy_status: string;
  featured: boolean;
  schoolId: number;
  campusId: number;
}

export interface CampusAvailability {
  campusName: string;
  availableFrom: string;
}

export interface NewCopyRow {
  condition: string;
  copy_status: string;
}

export interface CampusCopyGroup {
  campusId: number;
  copyCount: number;
  rows: NewCopyRow[];
}

export interface BookImportRow {
  rowNumber: number;
  title: string;
  author: string;
  isbn: string;
  conditionNieuw: number;
  conditionGoed: number;
  conditionGebruikt: number;
  conditionBeschadigd: number;
  campus: string;
  existingIsbn: string;
  updateExistingIsbn: boolean;
}

export interface EnrichedRowState {
  original: BookImportRow;
  titleInput: string;
  authorInput: string;
  isbnInput: string;
  campusInput: string;
  conditionNieuw: number;
  conditionGoed: number;
  conditionGebruikt: number;
  conditionBeschadigd: number;
  updateExistingIsbn: boolean;
  validationErrors: string[];
  invalidFields: Set<string>;
}

export interface RowError {
  rowNumber: number;
  message: string;
}

export interface CsvPreviewDTO {
  criticalErrors: string[];
  errors: RowError[];
  rows: BookImportRow[];
}

export interface CsvConfirmDTO {
  schoolId: number;
  rows: BookImportRow[];
}

export interface CsvImportDTO {
  errors: RowError[];
  importedBooks: number;
  importedCopies: number;
  copyIds: number[];
}

export interface CarouselSlot {
  book: Book;
  index: number;
  position: 'left-2' | 'left-1' | 'center' | 'right-1' | 'right-2';
}

export interface ReadingList {
  id: number;
  title: string;
  description: string;
  userFirstName: string;
  userLastName: string;
  items: Book[];
}

export interface CreateReadingListPayload {
  title: string;
  description: string;
  userId: number;
  itemIds: number[];
}

export interface Collection {
  id: number;
  title: string;
  description: string;
  featured: boolean;
  globallyFeatured: boolean;
  items: Book[];
  school: School | null;
}

export interface CreateCollectionPayload {
  title: string;
  description: string;
  schoolId: number;
  featured: boolean;
  globallyFeatured: boolean;
  itemIds: number[];
}

export interface Lending {
  id: number;
  borrowerName: string;
  borrowerId: number;
  bookTitle: string;
  bookAuthor: string;
  itemId?: number | null;
  schoolId: number;
  lendingDate: string;
  returnDate: string;
  dateReturned: string | null;
  returned: boolean;
  copyBarcode: string;
  extended: boolean;
}

export interface LendingRequest {
  borrowerId: number;
  copyBarcode: string;
  schoolId: number;
  lendingDate: string;
  returnDate: string;
}

export interface MonthlyLendingDTO {
  month: string;
  count: number;
}

export interface CampusStatisticsDTO {
  name: string;
  totalCopies: number;
  availableCopies: number;
  lendingCount: number;
  activeLendings: number;
  avgDuration: number;
  lendingsPerCopy: number;
  unavailableDays: number;
}

export interface RecentLendingDTO {
  id: number;
  borrowerName: string;
  lendingDate: string;
  returnDate: string;
  dateReturned: string | null;
  returned: boolean;
  extended: boolean;
}

export interface SchoolRankingDTO {
  rankAbsolute: number;
  rankPerCopy: number;
  rankReadingLevel: number | null;
  totalBooks: number;
  totalBooksReadingLevel: number | null;
  lendingsPerCopy: number;
}

export interface BookStatisticsDTO {
  totalLendings: number;
  activeLendings: number;
  avgDurationDays: number;
  lateReturnPercent: number;
  extensionPercent: number;
  globalUnavailableDays: number;
  lendingsByMonth: MonthlyLendingDTO[];
  campusStats: CampusStatisticsDTO[];
  recentLendings: RecentLendingDTO[];
  schoolRanking: SchoolRankingDTO;
}

export enum Role {
  LEERLING,
  LEERKRACHT,
  BIBLIOTHEEKBEHEERDER,
  ADMIN,
}

export enum CopyStatus {
  BESCHIKBAAR,
  UITGELEEND,
  ONTBREKEND,
}

export enum ItemCondition {
  NIEUW,
  GOED,
  GEBRUIKT,
  BESCHADIGD,
}

export interface Review {
  id: number;
  description: string | null;
  stars: number;
  createdAt: string;
  authorName: string;
  authorEmail?: string;
  authorSmartschoolUsername?: string;
  isAnonymous?: boolean;
  reportCount?: number;
}

export interface CreateReview {
  description?: string;
  stars: number;
  isAnonymous?: boolean;
}

export interface CreateReviewReport {
  reason?: string;
}

export interface BarcodePrintItem {
  copy: SchoolItemCopy;
  selected: boolean;
  imageUrl: string | null;
}

export interface BookSelectorItem {
  id?: string | number;
  bookId?: string | number;
  title?: string;
  bookTitle?: string;
  author?: string;
  authorName?: string;
  isbn?: string;
}

export interface ConfirmDialogAction {
  label: string;
  type?: 'primary' | 'danger' | 'cancel';
  value: string;
}

export type LendingStatus = 'OVERDUE' | 'DUE_SOON' | 'OK' | 'RETURNED';

export type LendingFilter = 'ALL' | LendingStatus;

export interface Class {
  className: string;
  schoolId: number
}
