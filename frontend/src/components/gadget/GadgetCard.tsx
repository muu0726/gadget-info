'use client';

import { useState } from 'react';
import { Heart } from 'lucide-react';
import Image from 'next/image';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { Gadget } from '@/types/gadget';

interface GadgetCardProps {
    gadget: Gadget;
    isFavorite: boolean;
    onToggleFavorite: (id: string) => void;
    size?: 'normal' | 'large';
}

export function GadgetCard({
    gadget,
    isFavorite,
    onToggleFavorite,
    size = 'normal',
}: GadgetCardProps) {
    const isLarge = size === 'large';
    const [imgSrc, setImgSrc] = useState(gadget.imageUrl);

    return (
        <Card
            className={`group relative overflow-hidden border-zinc-800 bg-zinc-900/50 backdrop-blur-sm transition-all duration-300 hover:border-zinc-700 hover:bg-zinc-900/80 ${isLarge ? 'md:col-span-2 md:row-span-2' : ''
                }`}
        >
            <div className={`relative overflow-hidden ${isLarge ? 'h-64' : 'h-40'}`}>
                <Image
                    src={imgSrc}
                    alt={gadget.title}
                    fill
                    className="object-cover transition-transform duration-500 group-hover:scale-105"
                    sizes={isLarge ? '(max-width: 768px) 100vw, 50vw' : '(max-width: 768px) 100vw, 33vw'}
                    onError={() => setImgSrc(`https://placehold.co/600x400/1a1a1a/666666?text=${gadget.category}`)}
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />

                {/* Badges */}
                <div className="absolute top-3 left-3 flex gap-2">
                    {gadget.isTrending && (
                        <Badge className="bg-orange-500/90 text-white hover:bg-orange-500">
                            🔥 Trend
                        </Badge>
                    )}
                    <Badge variant="secondary" className="bg-zinc-800/90 text-zinc-300">
                        {gadget.category}
                    </Badge>
                </div>

                {/* Favorite button */}
                <button
                    onClick={(e) => {
                        e.preventDefault();
                        onToggleFavorite(gadget.id);
                    }}
                    className="absolute top-3 right-3 p-2 rounded-full bg-black/40 backdrop-blur-sm transition-all hover:bg-black/60"
                    aria-label={isFavorite ? 'お気に入りから削除' : 'お気に入りに追加'}
                >
                    <Heart
                        className={`w-5 h-5 transition-colors ${isFavorite ? 'fill-red-500 text-red-500' : 'text-white'
                            }`}
                    />
                </button>
            </div>

            <CardContent className="p-4">
                <div className="space-y-2">
                    <div className="flex items-start justify-between gap-2">
                        <h3
                            className={`font-semibold text-white line-clamp-2 ${isLarge ? 'text-xl' : 'text-base'
                                }`}
                        >
                            {gadget.title}
                        </h3>
                        {gadget.price !== null && (
                            <span className="shrink-0 text-emerald-400 font-bold">
                                {gadget.priceText}
                            </span>
                        )}
                    </div>

                    <p
                        className={`text-zinc-400 line-clamp-2 ${isLarge ? 'text-sm' : 'text-xs'
                            }`}
                    >
                        {gadget.summary}
                    </p>

                    <div className="flex items-center justify-between pt-2">
                        <span className="text-xs text-zinc-500">{gadget.sourceName}</span>
                        <a
                            href={gadget.sourceUrl.includes('example.com')
                                ? `https://www.google.com/search?q=${encodeURIComponent(`${gadget.title} ${gadget.sourceName}`)}`
                                : gadget.sourceUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-xs text-zinc-400 hover:text-white transition-colors"
                        >
                            詳細を見る →
                        </a>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}
