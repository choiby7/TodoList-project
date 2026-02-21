import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { format, isToday, isTomorrow, isPast } from 'date-fns';
import { TodoPriority } from '@/types';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatDate(date: string | Date): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return format(dateObj, 'yyyy-MM-dd HH:mm');
}

export function formatDateShort(date: string | Date): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return format(dateObj, 'yyyy-MM-dd');
}

export function getDueDateStyle(dueDate: string | null | undefined): string {
  if (!dueDate) return 'text-gray-500';

  const due = new Date(dueDate);

  if (isPast(due) && !isToday(due)) {
    return 'text-red-600 font-medium'; // 기한 초과
  }

  if (isToday(due)) {
    return 'text-red-500'; // 오늘
  }

  if (isTomorrow(due)) {
    return 'text-orange-500'; // 내일
  }

  return 'text-gray-500'; // 미래
}

export function getDueDateText(dueDate: string | null | undefined): string {
  if (!dueDate) return '';

  const due = new Date(dueDate);

  if (isPast(due) && !isToday(due)) {
    return '기한 초과';
  }

  if (isToday(due)) {
    return '오늘';
  }

  if (isTomorrow(due)) {
    return '내일';
  }

  return formatDateShort(dueDate);
}

export const priorityStyles: Record<TodoPriority, string> = {
  HIGH: 'bg-red-100 text-red-700 border border-red-300',
  MEDIUM: 'bg-yellow-100 text-yellow-700 border border-yellow-300',
  LOW: 'bg-green-100 text-green-700 border border-green-300',
};

export const priorityLabels: Record<TodoPriority, string> = {
  HIGH: '높음',
  MEDIUM: '보통',
  LOW: '낮음',
};

export const statusLabels = {
  TODO: '할 일',
  IN_PROGRESS: '진행 중',
  COMPLETED: '완료',
};
