'use client';

import { TodoResponse } from '@/types';
import { TodoItem } from './TodoItem';

interface TodoListProps {
  todos: TodoResponse[];
  isLoading?: boolean;
}

export function TodoList({ todos, isLoading }: TodoListProps) {
  if (isLoading) {
    return (
      <div className="space-y-3">
        {[...Array(3)].map((_, i) => (
          <div
            key={i}
            className="h-24 rounded-lg border bg-gray-100 animate-pulse"
          />
        ))}
      </div>
    );
  }

  if (!todos || todos.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500">할 일이 없습니다</p>
        <p className="text-sm text-gray-400 mt-1">
          새로운 할 일을 추가해보세요
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {todos.map((todo) => (
        <TodoItem key={todo.todoId} todo={todo} />
      ))}
    </div>
  );
}
