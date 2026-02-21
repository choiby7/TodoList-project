import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { todosApi } from '@/api/todos';
import { todoKeys } from '@/lib/query-keys';
import { handleApiError } from '@/lib/error-handler';
import {
  TodoCreateRequest,
  TodoUpdateRequest,
  TodoFilters,
  TodoResponse,
  PageResponse,
  TodoStatus,
  TodoPriority,
} from '@/types';

// Todo 목록 조회
export function useTodos(filters: TodoFilters = {}) {
  return useQuery({
    queryKey: todoKeys.list(filters),
    queryFn: async () => {
      const response = await todosApi.getTodos(filters);
      return response.data;
    },
  });
}

// Todo 생성 (Optimistic Update)
export function useCreateTodo() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: TodoCreateRequest) => todosApi.createTodo(data),
    onMutate: async (newTodo) => {
      // 1. 진행 중인 쿼리 취소
      await queryClient.cancelQueries({ queryKey: todoKeys.lists() });

      // 2. 이전 데이터 저장
      const previousQueries = queryClient.getQueriesData<PageResponse<TodoResponse>>({
        queryKey: todoKeys.lists(),
      });

      // 3. 즉시 UI 업데이트 (임시 ID로 새 Todo 추가)
      queryClient.setQueriesData<PageResponse<TodoResponse>>(
        { queryKey: todoKeys.lists() },
        (old) => {
          if (!old) return old;

          const optimisticTodo: TodoResponse = {
            todoId: Date.now(), // 임시 ID
            userId: 0, // 서버에서 설정됨
            categoryId: newTodo.categoryId,
            title: newTodo.title,
            description: newTodo.description,
            priority: newTodo.priority || TodoPriority.MEDIUM,
            status: newTodo.status || TodoStatus.TODO,
            dueDate: newTodo.dueDate,
            isImportant: newTodo.isImportant || false,
            isDeleted: false,
            displayOrder: 0,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          };

          return {
            ...old,
            content: [optimisticTodo, ...old.content],
            totalElements: old.totalElements + 1,
          };
        }
      );

      return { previousQueries };
    },
    onError: (error, newTodo, context) => {
      // 4. 실패 시 롤백
      if (context?.previousQueries) {
        context.previousQueries.forEach(([queryKey, data]) => {
          queryClient.setQueryData(queryKey, data);
        });
      }
      const message = handleApiError(error);
      toast.error('생성 실패', { description: message });
    },
    onSettled: () => {
      // 5. 성공/실패 무관 재검증
      queryClient.invalidateQueries({ queryKey: todoKeys.lists() });
    },
    onSuccess: () => {
      toast.success('할 일이 생성되었습니다');
    },
  });
}

// Todo 수정 (Optimistic Update)
export function useUpdateTodo() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: TodoUpdateRequest }) =>
      todosApi.updateTodo(id, data),
    onMutate: async ({ id, data }) => {
      // 1. 진행 중인 쿼리 취소
      await queryClient.cancelQueries({ queryKey: todoKeys.lists() });

      // 2. 이전 데이터 저장
      const previousQueries = queryClient.getQueriesData<PageResponse<TodoResponse>>({
        queryKey: todoKeys.lists(),
      });

      // 3. 즉시 UI 업데이트
      queryClient.setQueriesData<PageResponse<TodoResponse>>(
        { queryKey: todoKeys.lists() },
        (old) => {
          if (!old) return old;

          return {
            ...old,
            content: old.content.map((todo) =>
              todo.todoId === id
                ? {
                    ...todo,
                    ...(data.title !== undefined && { title: data.title }),
                    ...(data.description !== undefined && { description: data.description }),
                    ...(data.priority !== undefined && { priority: data.priority }),
                    ...(data.status !== undefined && {
                      status: data.status,
                      completedAt: data.status === TodoStatus.COMPLETED
                        ? new Date().toISOString()
                        : undefined
                    }),
                    ...(data.dueDate !== undefined && { dueDate: data.dueDate }),
                    ...(data.categoryId !== undefined && { categoryId: data.categoryId }),
                    ...(data.isImportant !== undefined && { isImportant: data.isImportant }),
                    updatedAt: new Date().toISOString(),
                  }
                : todo
            ),
          };
        }
      );

      return { previousQueries };
    },
    onError: (error, variables, context) => {
      // 4. 실패 시 롤백
      if (context?.previousQueries) {
        context.previousQueries.forEach(([queryKey, data]) => {
          queryClient.setQueryData(queryKey, data);
        });
      }
      const message = handleApiError(error);
      toast.error('수정 실패', { description: message });
    },
    onSettled: () => {
      // 5. 성공/실패 무관 재검증
      queryClient.invalidateQueries({ queryKey: todoKeys.lists() });
    },
    onSuccess: () => {
      toast.success('할 일이 수정되었습니다');
    },
  });
}

// Todo 완료 토글 (Optimistic Update)
export function useToggleTodo() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => todosApi.toggleComplete(id),
    onMutate: async (id) => {
      // 1. 진행 중인 쿼리 취소
      await queryClient.cancelQueries({ queryKey: todoKeys.lists() });

      // 2. 이전 데이터 저장
      const previousQueries = queryClient.getQueriesData<PageResponse<TodoResponse>>({
        queryKey: todoKeys.lists(),
      });

      // 3. 즉시 UI 업데이트
      queryClient.setQueriesData<PageResponse<TodoResponse>>(
        { queryKey: todoKeys.lists() },
        (old) => {
          if (!old) return old;

          return {
            ...old,
            content: old.content.map((todo) =>
              todo.todoId === id
                ? {
                    ...todo,
                    status:
                      todo.status === TodoStatus.COMPLETED
                        ? TodoStatus.TODO
                        : TodoStatus.COMPLETED,
                    completedAt:
                      todo.status === TodoStatus.COMPLETED
                        ? undefined
                        : new Date().toISOString(),
                  }
                : todo
            ),
          };
        }
      );

      return { previousQueries };
    },
    onError: (error, id, context) => {
      // 4. 실패 시 롤백
      if (context?.previousQueries) {
        context.previousQueries.forEach(([queryKey, data]) => {
          queryClient.setQueryData(queryKey, data);
        });
      }
      const message = handleApiError(error);
      toast.error('변경 실패', { description: message });
    },
    onSettled: () => {
      // 5. 성공/실패 무관 재검증
      queryClient.invalidateQueries({ queryKey: todoKeys.lists() });
    },
  });
}

// Todo 삭제 (Optimistic Update)
export function useDeleteTodo() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => todosApi.deleteTodo(id),
    onMutate: async (id) => {
      await queryClient.cancelQueries({ queryKey: todoKeys.lists() });

      const previousQueries = queryClient.getQueriesData<PageResponse<TodoResponse>>({
        queryKey: todoKeys.lists(),
      });

      // 즉시 UI에서 제거
      queryClient.setQueriesData<PageResponse<TodoResponse>>(
        { queryKey: todoKeys.lists() },
        (old) => {
          if (!old) return old;

          return {
            ...old,
            content: old.content.filter((todo) => todo.todoId !== id),
            totalElements: old.totalElements - 1,
          };
        }
      );

      return { previousQueries };
    },
    onError: (error, id, context) => {
      if (context?.previousQueries) {
        context.previousQueries.forEach(([queryKey, data]) => {
          queryClient.setQueryData(queryKey, data);
        });
      }
      const message = handleApiError(error);
      toast.error('삭제 실패', { description: message });
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: todoKeys.lists() });
      queryClient.invalidateQueries({ queryKey: todoKeys.trash() });
    },
    onSuccess: () => {
      toast.success('휴지통으로 이동했습니다');
    },
  });
}

// 휴지통 목록 조회
export function useTrashTodos() {
  return useQuery({
    queryKey: todoKeys.trash(),
    queryFn: async () => {
      const response = await todosApi.getTrashTodos();
      return response.data;
    },
  });
}

// Todo 복원
export function useRestoreTodo() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => todosApi.restoreTodo(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: todoKeys.trash() });
      queryClient.invalidateQueries({ queryKey: todoKeys.lists() });
      toast.success('복원되었습니다');
    },
    onError: (error) => {
      const message = handleApiError(error);
      toast.error('복원 실패', { description: message });
    },
  });
}

// Todo 영구 삭제
export function usePermanentDeleteTodo() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => todosApi.permanentDeleteTodo(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: todoKeys.trash() });
      toast.success('영구 삭제되었습니다');
    },
    onError: (error) => {
      const message = handleApiError(error);
      toast.error('삭제 실패', { description: message });
    },
  });
}

// 휴지통 비우기
export function useEmptyTrash() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => todosApi.emptyTrash(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: todoKeys.trash() });
      toast.success('휴지통이 비워졌습니다');
    },
    onError: (error) => {
      const message = handleApiError(error);
      toast.error('실패', { description: message });
    },
  });
}
