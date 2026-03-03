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
  email: string;
  role: string;
  actiefSinceDate: string;
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