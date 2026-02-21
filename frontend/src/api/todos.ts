import apiClient from './axios-instance';
import {
  ApiResponse,
  PageResponse,
  TodoResponse,
  TodoCreateRequest,
  TodoUpdateRequest,
  TodoFilters,
} from '@/types';

export const todosApi = {
  // Todo 목록 조회 (페이징 + 필터링)
  getTodos: async (
    filters: TodoFilters = {}
  ): Promise<ApiResponse<PageResponse<TodoResponse>>> => {
    const params = new URLSearchParams();

    if (filters.page !== undefined) params.append('page', filters.page.toString());
    if (filters.size !== undefined) params.append('size', filters.size.toString());
    if (filters.status) params.append('status', filters.status);
    if (filters.priority) params.append('priority', filters.priority);
    if (filters.categoryId) params.append('categoryId', filters.categoryId.toString());
    if (filters.keyword) params.append('keyword', filters.keyword);
    if (filters.sortBy) params.append('sortBy', filters.sortBy);
    if (filters.sortOrder) params.append('sortOrder', filters.sortOrder);
    if (filters.dueFrom) params.append('dueFrom', filters.dueFrom);
    if (filters.dueTo) params.append('dueTo', filters.dueTo);

    const response = await apiClient.get(`/api/v1/todos?${params.toString()}`);
    return response.data;
  },

  // Todo 단건 조회
  getTodoById: async (id: number): Promise<ApiResponse<TodoResponse>> => {
    const response = await apiClient.get(`/api/v1/todos/${id}`);
    return response.data;
  },

  // Todo 생성
  createTodo: async (
    data: TodoCreateRequest
  ): Promise<ApiResponse<TodoResponse>> => {
    const response = await apiClient.post('/api/v1/todos', data);
    return response.data;
  },

  // Todo 수정
  updateTodo: async (
    id: number,
    data: TodoUpdateRequest
  ): Promise<ApiResponse<TodoResponse>> => {
    const response = await apiClient.put(`/api/v1/todos/${id}`, data);
    return response.data;
  },

  // Todo 삭제 (소프트 삭제)
  deleteTodo: async (id: number): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete(`/api/v1/todos/${id}`);
    return response.data;
  },

  // Todo 완료 상태 토글
  toggleComplete: async (id: number): Promise<ApiResponse<TodoResponse>> => {
    const response = await apiClient.patch(`/api/v1/todos/${id}/toggle`);
    return response.data;
  },

  // 휴지통 목록 조회
  getTrashTodos: async (): Promise<ApiResponse<PageResponse<TodoResponse>>> => {
    const response = await apiClient.get('/api/v1/todos/trash');
    return response.data;
  },

  // Todo 복원
  restoreTodo: async (id: number): Promise<ApiResponse<TodoResponse>> => {
    const response = await apiClient.patch(`/api/v1/todos/${id}/restore`);
    return response.data;
  },

  // Todo 영구 삭제
  permanentDeleteTodo: async (id: number): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete(`/api/v1/todos/${id}/permanent`);
    return response.data;
  },

  // 휴지통 비우기
  emptyTrash: async (): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete('/api/v1/todos/trash/empty');
    return response.data;
  },
};
