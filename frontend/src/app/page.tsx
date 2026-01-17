'use client';

import { useState, useMemo } from 'react';
import { Heart, Filter, Grid3x3 } from 'lucide-react';
import { Header } from '@/components/layout/Header';
import { Footer } from '@/components/layout/Footer';
import { GadgetCard } from '@/components/gadget/GadgetCard';
import { CategoryTabs } from '@/components/filters/CategoryTabs';
import { PriceSlider } from '@/components/filters/PriceSlider';
import { Button } from '@/components/ui/button';
import { Toggle } from '@/components/ui/toggle';
import { useFavorites } from '@/hooks/useFavorites';
import { useFilters } from '@/hooks/useFilters';
import { mockData } from '@/data/mock-data';

export default function Home() {
  const { favorites, toggleFavorite, isFavorite, isLoaded } = useFavorites();
  const {
    category,
    setCategory,
    priceRange,
    setPriceRange,
    showFavoritesOnly,
    setShowFavoritesOnly,
    filterByFavorites,
  } = useFilters({ gadgets: mockData.gadgets });

  const [showFilters, setShowFilters] = useState(false);

  const displayedGadgets = useMemo(() => {
    return filterByFavorites(favorites);
  }, [filterByFavorites, favorites]);

  // トレンドのガジェットは大きく表示
  const trendingIds = mockData.gadgets
    .filter((g) => g.isTrending)
    .map((g) => g.id);

  return (
    <div className="min-h-screen flex flex-col bg-black">
      <Header />

      <main className="flex-1 container mx-auto px-4 py-8">
        {/* Filters Section */}
        <div className="space-y-6 mb-8">
          {/* Category Tabs */}
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <CategoryTabs value={category} onChange={setCategory} />

            <div className="flex items-center gap-2">
              <Toggle
                pressed={showFavoritesOnly}
                onPressedChange={setShowFavoritesOnly}
                aria-label="お気に入りのみ表示"
                className="data-[state=on]:bg-red-500/20 data-[state=on]:text-red-400"
              >
                <Heart className="w-4 h-4 mr-1" />
                お気に入り
                {favorites.length > 0 && (
                  <span className="ml-1 text-xs">({favorites.length})</span>
                )}
              </Toggle>

              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowFilters(!showFilters)}
                className="border-zinc-700 hover:bg-zinc-800"
              >
                <Filter className="w-4 h-4 mr-1" />
                フィルター
              </Button>
            </div>
          </div>

          {/* Advanced Filters */}
          {showFilters && (
            <div className="p-4 bg-zinc-900 border border-zinc-800 rounded-xl space-y-4">
              <PriceSlider value={priceRange} onChange={setPriceRange} />
            </div>
          )}

          {/* Results Count */}
          <div className="flex items-center gap-2 text-sm text-zinc-400">
            <Grid3x3 className="w-4 h-4" />
            <span>{displayedGadgets.length}件の製品を表示中</span>
            {showFavoritesOnly && (
              <span className="text-red-400">（お気に入りのみ）</span>
            )}
          </div>
        </div>

        {/* Bento Grid */}
        {isLoaded && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 auto-rows-auto">
            {displayedGadgets.map((gadget, index) => (
              <GadgetCard
                key={gadget.id}
                gadget={gadget}
                isFavorite={isFavorite(gadget.id)}
                onToggleFavorite={toggleFavorite}
                size={trendingIds.includes(gadget.id) && index < 2 ? 'large' : 'normal'}
              />
            ))}
          </div>
        )}

        {/* Empty State */}
        {isLoaded && displayedGadgets.length === 0 && (
          <div className="text-center py-20">
            <div className="text-6xl mb-4">🔍</div>
            <h3 className="text-xl font-semibold text-white mb-2">
              該当する製品がありません
            </h3>
            <p className="text-zinc-400">
              フィルター条件を変更してみてください
            </p>
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
}
