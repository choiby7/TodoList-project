'use client';

import { useState, useEffect } from 'react';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { TodoFilters as TodoFiltersType, TodoPriority, TodoStatus } from '@/types';
import { Search } from 'lucide-react';
import { priorityLabels, statusLabels } from '@/lib/utils';

interface TodoFiltersProps {
  filters: TodoFiltersType;
  onFiltersChange: (filters: TodoFiltersType) => void;
}

export function TodoFilters({ filters, onFiltersChange }: TodoFiltersProps) {
  const [keyword, setKeyword] = useState(filters.keyword || '');

  // 300ms 디바운스
  useEffect(() => {
    const timer = setTimeout(() => {
      onFiltersChange({ ...filters, keyword: keyword || undefined });
    }, 300);

    return () => clearTimeout(timer);
  }, [keyword]);

  return (
    <div className="space-y-4">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
        <Input
          placeholder="할 일 검색..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          className="pl-10"
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <Select
          value={filters.status || 'all'}
          onValueChange={(value) =>
            onFiltersChange({
              ...filters,
              status: value === 'all' ? undefined : (value as TodoStatus),
            })
          }
        >
          <SelectTrigger>
            <SelectValue placeholder="상태" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">전체</SelectItem>
            {Object.values(TodoStatus).map((s) => (
              <SelectItem key={s} value={s}>
                {statusLabels[s]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select
          value={filters.priority || 'all'}
          onValueChange={(value) =>
            onFiltersChange({
              ...filters,
              priority: value === 'all' ? undefined : (value as TodoPriority),
            })
          }
        >
          <SelectTrigger>
            <SelectValue placeholder="우선순위" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">전체</SelectItem>
            {Object.values(TodoPriority).map((p) => (
              <SelectItem key={p} value={p}>
                {priorityLabels[p]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
    </div>
  );
}
