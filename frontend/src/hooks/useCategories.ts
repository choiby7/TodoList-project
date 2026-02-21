import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { categoriesApi } from '@/api/categories';
import { categoryKeys } from '@/lib/query-keys';
import { handleApiError } from '@/lib/error-handler';
import { CategoryRequest } from '@/types';

// 카테고리 목록 조회
export function useCategories() {
  return useQuery({
    queryKey: categoryKeys.lists(),
    queryFn: async () => {
      const response = await categoriesApi.getCategories();
      return response.data;
    },
    staleTime: 10 * 60 * 1000, // 10분
  });
}

// 카테고리 생성
export function useCreateCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CategoryRequest) => categoriesApi.createCategory(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: categoryKeys.lists() });
      toast.success('카테고리가 생성되었습니다');
    },
    onError: (error) => {
      const message = handleApiError(error);
      toast.error('생성 실패', { description: message });
    },
  });
}

// 카테고리 수정
export function useUpdateCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: CategoryRequest }) =>
      categoriesApi.updateCategory(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: categoryKeys.lists() });
      toast.success('카테고리가 수정되었습니다');
    },
    onError: (error) => {
      const message = handleApiError(error);
      toast.error('수정 실패', { description: message });
    },
  });
}

// 카테고리 삭제
export function useDeleteCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => categoriesApi.deleteCategory(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: categoryKeys.lists() });
      toast.success('카테고리가 삭제되었습니다');
    },
    onError: (error) => {
      const message = handleApiError(error);
      toast.error('삭제 실패', { description: message });
    },
  });
}
