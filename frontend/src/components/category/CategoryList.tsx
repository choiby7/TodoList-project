'use client';

import { Plus, Folder } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { useCategories } from '@/hooks/useCategories';

interface CategoryListProps {
  onCreateClick: () => void;
}

export function CategoryList({ onCreateClick }: CategoryListProps) {
  const { data: categories, isLoading } = useCategories();

  if (isLoading) {
    return (
      <div className="space-y-2">
        {[...Array(3)].map((_, i) => (
          <div
            key={i}
            className="h-10 rounded-md bg-gray-100 animate-pulse"
          />
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between px-2">
        <h3 className="text-sm font-semibold text-gray-600">카테고리</h3>
        <Button
          variant="ghost"
          size="sm"
          onClick={onCreateClick}
          className="h-8 w-8 p-0"
        >
          <Plus className="h-4 w-4" />
        </Button>
      </div>

      <div className="space-y-1">
        {categories?.map((category) => (
          <button
            key={category.categoryId}
            className="w-full flex items-center gap-2 px-3 py-2 rounded-md text-sm hover:bg-gray-100 transition-colors"
          >
            <Folder
              className="h-4 w-4"
              style={{ color: category.colorCode }}
            />
            <span className="flex-1 text-left">{category.name}</span>
            {category.todoCount !== undefined && category.todoCount > 0 && (
              <Badge variant="secondary" className="text-xs">
                {category.todoCount}
              </Badge>
            )}
          </button>
        ))}

        {(!categories || categories.length === 0) && (
          <p className="text-sm text-gray-400 px-3 py-2">
            카테고리가 없습니다
          </p>
        )}
      </div>
    </div>
  );
}
