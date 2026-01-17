'use client';

import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';

import type { CategoryFilter } from '@/hooks/useFilters';

interface CategoryTabsProps {
    value: CategoryFilter;
    onChange: (category: CategoryFilter) => void;
}

const categories: { value: CategoryFilter; label: string; emoji: string }[] = [
    { value: 'All', label: 'すべて', emoji: '📱' },
    { value: 'Mobile', label: 'Mobile', emoji: '📲' },
    { value: 'PC', label: 'PC', emoji: '💻' },
    { value: 'Wearable', label: 'Wearable', emoji: '⌚' },
    { value: 'Audio', label: 'Audio', emoji: '🎧' },
    { value: 'Smart Home', label: 'Smart Home', emoji: '🏠' },
];

export function CategoryTabs({ value, onChange }: CategoryTabsProps) {
    return (
        <Tabs value={value} onValueChange={(v) => onChange(v as CategoryFilter)}>
            <TabsList className="bg-zinc-900 border border-zinc-800 p-1 h-auto flex-wrap gap-1">
                {categories.map((cat) => (
                    <TabsTrigger
                        key={cat.value}
                        value={cat.value}
                        className="data-[state=active]:bg-zinc-700 data-[state=active]:text-white px-4 py-2 text-zinc-400 hover:text-white transition-colors"
                    >
                        <span className="mr-1">{cat.emoji}</span>
                        {cat.label}
                    </TabsTrigger>
                ))}
            </TabsList>
        </Tabs>
    );
}
