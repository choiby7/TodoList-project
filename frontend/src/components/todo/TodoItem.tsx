'use client';

import { useState } from 'react';
import { TodoResponse, TodoUpdateRequest } from '@/types';
import { Checkbox } from '@/components/ui/checkbox';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Trash2, Edit } from 'lucide-react';
import { useToggleTodo, useDeleteTodo, useUpdateTodo } from '@/hooks/useTodos';
import { TodoForm } from './TodoForm';
import { cn, priorityStyles, priorityLabels, getDueDateStyle, getDueDateText } from '@/lib/utils';

interface TodoItemProps {
  todo: TodoResponse;
}

export function TodoItem({ todo }: TodoItemProps) {
  const [isEditOpen, setIsEditOpen] = useState(false);
  const toggleMutation = useToggleTodo();
  const deleteMutation = useDeleteTodo();
  const updateMutation = useUpdateTodo();

  const handleToggle = () => {
    toggleMutation.mutate(todo.todoId);
  };

  const handleDelete = () => {
    deleteMutation.mutate(todo.todoId);
  };

  const handleUpdate = (data: TodoUpdateRequest) => {
    console.log('handleUpdate called with data:', data);
    updateMutation.mutate(
      { id: todo.todoId, data },
      {
        onSuccess: () => {
          console.log('Update successful, closing dialog');
          setIsEditOpen(false);
        },
        onError: (error) => {
          console.error('Update failed:', error);
        },
      }
    );
  };

  const isCompleted = todo.status === 'COMPLETED';

  return (
    <div
      className={cn(
        'flex items-start gap-3 rounded-lg border p-4 transition-colors hover:bg-gray-50',
        isCompleted && 'opacity-60'
      )}
    >
      <Checkbox
        checked={isCompleted}
        onCheckedChange={handleToggle}
        disabled={toggleMutation.isPending}
        className="mt-1"
      />

      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <div className="flex-1 min-w-0">
            <h3
              className={cn(
                'font-medium cursor-pointer hover:text-blue-600 transition-colors',
                isCompleted && 'line-through text-gray-500'
              )}
              onClick={() => setIsEditOpen(true)}
            >
              {todo.title}
            </h3>
            {todo.description && (
              <p
                className="mt-1 text-sm text-gray-600 line-clamp-2 cursor-pointer"
                onClick={() => setIsEditOpen(true)}
              >
                {todo.description}
              </p>
            )}
          </div>

          <div className="flex gap-1">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setIsEditOpen(true)}
              className="text-gray-400 hover:text-blue-600"
            >
              <Edit className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              onClick={handleDelete}
              disabled={deleteMutation.isPending}
              className="text-gray-400 hover:text-red-600"
            >
              <Trash2 className="h-4 w-4" />
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

      {/* 수정 다이얼로그 */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>할 일 수정</DialogTitle>
          </DialogHeader>
          <TodoForm
            onSubmit={handleUpdate}
            onCancel={() => setIsEditOpen(false)}
            isSubmitting={updateMutation.isPending}
            defaultValues={{
              title: todo.title,
              description: todo.description || undefined,
              priority: todo.priority,
              status: todo.status,
              dueDate: todo.dueDate || undefined,
              categoryId: todo.categoryId || undefined,
            }}
          />
        </DialogContent>
      </Dialog>
    </div>
  );
}
