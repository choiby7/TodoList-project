'use client';

import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { RotateCcw, Trash2, AlertCircle } from 'lucide-react';
import {
  useTrashTodos,
  useRestoreTodo,
  usePermanentDeleteTodo,
  useEmptyTrash,
} from '@/hooks/useTodos';
import { priorityStyles, priorityLabels, getDueDateStyle, getDueDateText } from '@/lib/utils';
import { cn } from '@/lib/utils';
import { TodoResponse } from '@/types';

export default function TrashPage() {
  const { data, isLoading } = useTrashTodos();
  const restoreMutation = useRestoreTodo();
  const permanentDeleteMutation = usePermanentDeleteTodo();
  const emptyTrashMutation = useEmptyTrash();

  // PageResponse에서 content 추출
  const todos = data?.content || [];

  const handleRestore = (id: number) => {
    restoreMutation.mutate(id);
  };

  const handlePermanentDelete = (id: number) => {
    if (confirm('정말로 영구 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      permanentDeleteMutation.mutate(id);
    }
  };

  const handleEmptyTrash = () => {
    if (confirm('휴지통을 비우시겠습니까? 모든 항목이 영구 삭제됩니다.')) {
      emptyTrashMutation.mutate();
    }
  };

  if (isLoading) {
    return (
      <div className="max-w-4xl mx-auto p-6">
        <div className="space-y-3">
          {[...Array(3)].map((_, i) => (
            <div
              key={i}
              className="h-24 rounded-lg border bg-gray-100 animate-pulse"
            />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold">휴지통</h1>
          <p className="text-gray-600 mt-1">
            {data?.totalElements || 0}개의 삭제된 항목
          </p>
        </div>

        {todos && todos.length > 0 && (
          <Button
            variant="destructive"
            onClick={handleEmptyTrash}
            disabled={emptyTrashMutation.isPending}
          >
            <Trash2 className="h-4 w-4 mr-2" />
            휴지통 비우기
          </Button>
        )}
      </div>

      {!todos || todos.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500">휴지통이 비어있습니다</p>
        </div>
      ) : (
        <div className="space-y-3">
          {todos.map((todo: TodoResponse) => (
            <div
              key={todo.todoId}
              className="flex items-start gap-3 rounded-lg border p-4 bg-red-50/50"
            >
              <AlertCircle className="h-5 w-5 text-red-600 mt-1" />

              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <h3 className="font-medium text-gray-900">{todo.title}</h3>
                    {todo.description && (
                      <p className="mt-1 text-sm text-gray-600 line-clamp-2">
                        {todo.description}
                      </p>
                    )}
                  </div>

                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleRestore(todo.todoId)}
                      disabled={restoreMutation.isPending}
                    >
                      <RotateCcw className="h-4 w-4 mr-1" />
                      복원
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => handlePermanentDelete(todo.todoId)}
                      disabled={permanentDeleteMutation.isPending}
                    >
                      <Trash2 className="h-4 w-4 mr-1" />
                      영구 삭제
                    </Button>
                  </div>
                </div>

                <div className="mt-2 flex flex-wrap items-center gap-2">
                  <Badge className={priorityStyles[todo.priority]}>
                    {priorityLabels[todo.priority]}
                  </Badge>

                  {todo.categoryName && (
                    <Badge variant="outline">{todo.categoryName}</Badge>
                  )}

                  {todo.dueDate && (
                    <span className={cn('text-xs', getDueDateStyle(todo.dueDate))}>
                      {getDueDateText(todo.dueDate)}
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
