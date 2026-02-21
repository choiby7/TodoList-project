'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { TodoPriority, TodoStatus } from '@/types';
import { priorityLabels, statusLabels } from '@/lib/utils';

const todoSchema = z.object({
  title: z
    .string()
    .min(1, '제목을 입력해주세요')
    .max(200, '제목은 최대 200자까지 가능합니다'),
  description: z
    .string()
    .max(5000, '설명은 최대 5000자까지 가능합니다')
    .transform((val) => val === '' ? undefined : val)
    .optional(),
  priority: z.nativeEnum(TodoPriority).optional(),
  status: z.nativeEnum(TodoStatus).optional(),
  dueDate: z
    .string()
    .transform((val) => val === '' ? undefined : val)
    .optional(),
  categoryId: z.number().optional(),
});

type TodoFormData = z.infer<typeof todoSchema>;

interface TodoFormProps {
  onSubmit: (data: TodoFormData) => void;
  onCancel: () => void;
  isSubmitting?: boolean;
  defaultValues?: Partial<TodoFormData>;
}

export function TodoForm({
  onSubmit,
  onCancel,
  isSubmitting,
  defaultValues,
}: TodoFormProps) {
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<TodoFormData>({
    resolver: zodResolver(todoSchema),
    defaultValues: {
      priority: TodoPriority.MEDIUM,
      status: TodoStatus.TODO,
      ...defaultValues,
    },
  });

  const priority = watch('priority');
  const status = watch('status');

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="title">제목 *</Label>
        <Input
          id="title"
          placeholder="할 일을 입력하세요"
          {...register('title')}
        />
        {errors.title && (
          <p className="text-sm text-red-600">{errors.title.message}</p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">설명</Label>
        <Textarea
          id="description"
          placeholder="상세 설명을 입력하세요 (선택사항)"
          rows={4}
          {...register('description')}
        />
        {errors.description && (
          <p className="text-sm text-red-600">{errors.description.message}</p>
        )}
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label>우선순위</Label>
          <Select
            value={priority}
            onValueChange={(value) => setValue('priority', value as TodoPriority)}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {Object.values(TodoPriority).map((p) => (
                <SelectItem key={p} value={p}>
                  {priorityLabels[p]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label>상태</Label>
          <Select
            value={status}
            onValueChange={(value) => setValue('status', value as TodoStatus)}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {Object.values(TodoStatus).map((s) => (
                <SelectItem key={s} value={s}>
                  {statusLabels[s]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="dueDate">마감일</Label>
        <Input
          id="dueDate"
          type="datetime-local"
          {...register('dueDate')}
        />
      </div>

      <div className="flex justify-end gap-2 pt-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          취소
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? '저장 중...' : '저장'}
        </Button>
      </div>
    </form>
  );
}
