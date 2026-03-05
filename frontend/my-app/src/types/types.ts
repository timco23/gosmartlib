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
  actiefSinceDate: string;
}

export interface CreateUserRequest {
  firstName: string;
  name: string;
  studentNumber: string;
  className: string;
  schoolId: number;
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

export interface ChangePasswordRequest {
  email: string;
  oldPassword: string;
  newPassword: string;
}
