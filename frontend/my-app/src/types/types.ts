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

export interface Tag {
  id: number;
  name: string;
  default: boolean;
}
