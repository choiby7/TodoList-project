'use client';

import { useState } from 'react';
import { Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { TodoList } from '@/components/todo/TodoList';
import { TodoForm } from '@/components/todo/TodoForm';
import { TodoFilters } from '@/components/todo/TodoFilters';
import { useTodos, useCreateTodo } from '@/hooks/useTodos';
import { TodoFilters as TodoFiltersType, TodoCreateRequest } from '@/types';

export default function DashboardPage() {
  const [filters, setFilters] = useState<TodoFiltersType>({
    page: 0,
    size: 20,
  });
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const { data, isLoading } = useTodos(filters);
  const createMutation = useCreateTodo();

  const handleCreate = (formData: TodoCreateRequest) => {
    createMutation.mutate(formData, {
      onSuccess: () => {
        setIsDialogOpen(false);
      },
    });
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold">할 일 목록</h1>
          <p className="text-gray-600 mt-1">
            {data?.totalElements || 0}개의 할 일
          </p>
        </div>

        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="h-4 w-4 mr-2" />
              새 할 일
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>새 할 일 만들기</DialogTitle>
            </DialogHeader>
            <TodoForm
              onSubmit={handleCreate}
              onCancel={() => setIsDialogOpen(false)}
              isSubmitting={createMutation.isPending}
            />
          </DialogContent>
        </Dialog>
      </div>

      <div className="mb-6">
        <TodoFilters filters={filters} onFiltersChange={setFilters} />
      </div>

      <TodoList
        todos={data?.content || []}
        isLoading={isLoading}
      />
    </div>
  );
}
