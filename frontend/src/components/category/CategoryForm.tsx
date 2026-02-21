'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

const categorySchema = z.object({
  name: z
    .string()
    .min(1, '카테고리 이름을 입력해주세요')
    .max(50, '이름은 최대 50자까지 가능합니다'),
  colorCode: z.string().regex(/^#[0-9A-F]{6}$/i, '유효한 색상 코드를 입력해주세요').optional(),
});

type CategoryFormData = z.infer<typeof categorySchema>;

interface CategoryFormProps {
  onSubmit: (data: CategoryFormData) => void;
  onCancel: () => void;
  isSubmitting?: boolean;
  defaultValues?: Partial<CategoryFormData>;
}

const presetColors = [
  '#3B82F6', // blue
  '#EF4444', // red
  '#10B981', // green
  '#F59E0B', // yellow
  '#8B5CF6', // purple
  '#EC4899', // pink
  '#6B7280', // gray
  '#14B8A6', // teal
];

export function CategoryForm({
  onSubmit,
  onCancel,
  isSubmitting,
  defaultValues,
}: CategoryFormProps) {
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<CategoryFormData>({
    resolver: zodResolver(categorySchema),
    defaultValues: {
      colorCode: '#3B82F6',
      ...defaultValues,
    },
  });

  const selectedColor = watch('colorCode');

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="name">카테고리 이름 *</Label>
        <Input
          id="name"
          placeholder="예: 업무, 개인, 공부"
          {...register('name')}
        />
        {errors.name && (
          <p className="text-sm text-red-600">{errors.name.message}</p>
        )}
      </div>

      <div className="space-y-2">
        <Label>색상</Label>
        <div className="flex gap-2 flex-wrap">
          {presetColors.map((color) => (
            <button
              key={color}
              type="button"
              onClick={() => setValue('colorCode', color)}
              className={`w-8 h-8 rounded-full border-2 transition-all ${
                selectedColor === color
                  ? 'border-gray-900 scale-110'
                  : 'border-gray-300'
              }`}
              style={{ backgroundColor: color }}
            />
          ))}
        </div>
        <Input
          type="text"
          placeholder="#3B82F6"
          {...register('colorCode')}
          className="mt-2"
        />
        {errors.colorCode && (
          <p className="text-sm text-red-600">{errors.colorCode.message}</p>
        )}
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
