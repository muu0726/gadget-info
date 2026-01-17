'use client';

import { useState, useMemo, useCallback } from 'react';
import type { GadgetCategory, Gadget } from '@/types/gadget';

export type CategoryFilter = GadgetCategory | 'All';

interface UseFiltersOptions {
    gadgets: Gadget[];
}

export function useFilters({ gadgets }: UseFiltersOptions) {
    const [category, setCategory] = useState<CategoryFilter>('All');
    const [priceRange, setPriceRange] = useState<[number, number]>([0, 300000]);
    const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);

    const filteredGadgets = useMemo(() => {
        return gadgets.filter((gadget) => {
            // カテゴリフィルター
            if (category !== 'All' && gadget.category !== category) {
                return false;
            }
            // 価格フィルター
            if (gadget.price !== null) {
                if (gadget.price < priceRange[0] || gadget.price > priceRange[1]) {
                    return false;
                }
            }
            return true;
        });
    }, [gadgets, category, priceRange]);

    const filterByFavorites = useCallback(
        (favorites: string[]) => {
            if (!showFavoritesOnly) return filteredGadgets;
            return filteredGadgets.filter((g) => favorites.includes(g.id));
        },
        [filteredGadgets, showFavoritesOnly]
    );

    return {
        category,
        setCategory,
        priceRange,
        setPriceRange,
        showFavoritesOnly,
        setShowFavoritesOnly,
        filteredGadgets,
        filterByFavorites,
    };
}
