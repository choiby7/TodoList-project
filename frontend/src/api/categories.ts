import apiClient from './axios-instance';
import {
  ApiResponse,
  CategoryResponse,
  CategoryRequest,
} from '@/types';

export const categoriesApi = {
  // 카테고리 목록 조회
  getCategories: async (): Promise<ApiResponse<CategoryResponse[]>> => {
    const response = await apiClient.get('/api/v1/categories');
    return response.data;
  },

  // 카테고리 생성
  createCategory: async (
    data: CategoryRequest
  ): Promise<ApiResponse<CategoryResponse>> => {
    const response = await apiClient.post('/api/v1/categories', data);
    return response.data;
  },

  // 카테고리 수정
  updateCategory: async (
    id: number,
    data: CategoryRequest
  ): Promise<ApiResponse<CategoryResponse>> => {
    const response = await apiClient.put(`/api/v1/categories/${id}`, data);
    return response.data;
  },

  // 카테고리 삭제
  deleteCategory: async (id: number): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete(`/api/v1/categories/${id}`);
    return response.data;
  },

  // 카테고리 순서 변경
  reorderCategories: async (
    categoryIds: number[]
  ): Promise<ApiResponse<void>> => {
    const response = await apiClient.put('/api/v1/categories/reorder', {
      categoryIds,
    });
    return response.data;
  },
};
