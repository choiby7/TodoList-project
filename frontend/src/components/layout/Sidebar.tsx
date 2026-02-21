'use client';

import { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Home, Trash2, X } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { CategoryList } from '@/components/category/CategoryList';
import { CategoryForm } from '@/components/category/CategoryForm';
import { useCreateCategory } from '@/hooks/useCategories';
import { useUiStore } from '@/store/uiStore';
import { CategoryRequest } from '@/types';

export function Sidebar() {
  const pathname = usePathname();
  const { sidebarOpen, setSidebarOpen } = useUiStore();
  const [isCategoryDialogOpen, setIsCategoryDialogOpen] = useState(false);
  const createCategoryMutation = useCreateCategory();

  const handleCreateCategory = (data: CategoryRequest) => {
    createCategoryMutation.mutate(data, {
      onSuccess: () => {
        setIsCategoryDialogOpen(false);
      },
    });
  };

  const navItems = [
    {
      href: '/dashboard',
      label: '대시보드',
      icon: Home,
    },
    {
      href: '/dashboard/trash',
      label: '휴지통',
      icon: Trash2,
    },
  ];

  return (
    <>
      {/* Mobile Overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          'fixed left-0 top-16 bottom-0 z-50 w-64 border-r bg-white transition-transform md:translate-x-0',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        <div className="flex h-full flex-col">
          {/* Mobile Close Button */}
          <div className="flex items-center justify-between px-4 py-3 border-b md:hidden">
            <h3 className="font-semibold">메뉴</h3>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setSidebarOpen(false)}
            >
              <X className="h-5 w-5" />
            </Button>
          </div>

          <nav className="flex-1 overflow-y-auto p-4 space-y-6">
            {/* Main Navigation */}
            <div className="space-y-1">
              {navItems.map((item) => {
                const isActive = pathname === item.href;
                const Icon = item.icon;

                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={cn(
                      'flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium transition-colors',
                      isActive
                        ? 'bg-blue-50 text-blue-600'
                        : 'text-gray-700 hover:bg-gray-100'
                    )}
                  >
                    <Icon className="h-5 w-5" />
                    {item.label}
                  </Link>
                );
              })}
            </div>

            {/* Categories */}
            <div>
              <CategoryList
                onCreateClick={() => setIsCategoryDialogOpen(true)}
              />
            </div>
          </nav>
        </div>
      </aside>

      {/* Category Create Dialog */}
      <Dialog open={isCategoryDialogOpen} onOpenChange={setIsCategoryDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>새 카테고리</DialogTitle>
          </DialogHeader>
          <CategoryForm
            onSubmit={handleCreateCategory}
            onCancel={() => setIsCategoryDialogOpen(false)}
            isSubmitting={createCategoryMutation.isPending}
          />
        </DialogContent>
      </Dialog>
    </>
  );
}
