// ========== Enums ==========
export enum TodoPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
}

export enum TodoStatus {
  TODO = 'TODO',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
}

// ========== API Response Types ==========
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface ErrorResponse {
  success: false;
  errorCode: string;
  message: string;
  timestamp: string;
  path?: string;
  errors?: Array<{
    field: string;
    message: string;
    rejectedValue: any;
  }>;
}

// ========== Auth Types ==========
export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  username: string;
  termsAgreed: boolean;
  privacyAgreed: boolean;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface UserResponse {
  userId: number;
  email: string;
  username: string;
  profileImageUrl?: string;
  isActive: boolean;
  emailVerified: boolean;
  provider?: 'GOOGLE' | 'GITHUB' | 'KAKAO' | null;
  providerId?: string | null;
  createdAt: string;
  lastLoginAt?: string;
}

// ========== Todo Types ==========
export interface TodoResponse {
  todoId: number;
  userId: number;
  categoryId?: number;
  categoryName?: string;
  title: string;
  description?: string;
  priority: TodoPriority;
  status: TodoStatus;
  dueDate?: string;
  reminderAt?: string;
  isImportant: boolean;
  isDeleted: boolean;
  displayOrder: number;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  deletedAt?: string;
}

export interface TodoCreateRequest {
  title: string;
  description?: string;
  priority?: TodoPriority;
  status?: TodoStatus;
  dueDate?: string;
  reminderAt?: string;
  isImportant?: boolean;
  categoryId?: number;
}

export interface TodoUpdateRequest {
  title?: string;
  description?: string;
  priority?: TodoPriority;
  status?: TodoStatus;
  dueDate?: string;
  reminderAt?: string;
  isImportant?: boolean;
  categoryId?: number;
}

export interface TodoFilters {
  page?: number;
  size?: number;
  status?: TodoStatus;
  priority?: TodoPriority;
  categoryId?: number;
  keyword?: string;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
  dueFrom?: string;
  dueTo?: string;
}

// ========== Category Types ==========
export interface CategoryResponse {
  categoryId: number;
  userId: number;
  name: string;
  colorCode: string;
  icon?: string;
  displayOrder: number;
  todoCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CategoryRequest {
  name: string;
  colorCode?: string;
  icon?: string;
}
