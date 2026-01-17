'use client';

import { useState, useEffect, useCallback } from 'react';

const FAVORITES_KEY = 'gadget-favorites';

export function useFavorites() {
    const [favorites, setFavorites] = useState<string[]>([]);
    const [isLoaded, setIsLoaded] = useState(false);

    useEffect(() => {
        const stored = localStorage.getItem(FAVORITES_KEY);
        if (stored) {
            try {
                setFavorites(JSON.parse(stored));
            } catch {
                setFavorites([]);
            }
        }
        setIsLoaded(true);
    }, []);

    useEffect(() => {
        if (isLoaded) {
            localStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites));
        }
    }, [favorites, isLoaded]);

    const toggleFavorite = useCallback((id: string) => {
        setFavorites((prev) =>
            prev.includes(id) ? prev.filter((fid) => fid !== id) : [...prev, id]
        );
    }, []);

    const isFavorite = useCallback(
        (id: string) => favorites.includes(id),
        [favorites]
    );

    return { favorites, toggleFavorite, isFavorite, isLoaded };
}
