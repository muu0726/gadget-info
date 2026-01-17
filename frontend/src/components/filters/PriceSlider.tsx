'use client';

import { Slider } from '@/components/ui/slider';

interface PriceSliderProps {
    value: [number, number];
    onChange: (value: [number, number]) => void;
    min?: number;
    max?: number;
}

function formatPrice(price: number): string {
    if (price >= 10000) {
        return `${(price / 10000).toFixed(1)}万円`;
    }
    return `${price.toLocaleString()}円`;
}

export function PriceSlider({
    value,
    onChange,
    min = 0,
    max = 300000,
}: PriceSliderProps) {
    return (
        <div className="space-y-3">
            <div className="flex items-center justify-between">
                <span className="text-sm text-zinc-400">価格帯</span>
                <span className="text-sm text-white font-medium">
                    {formatPrice(value[0])} 〜 {formatPrice(value[1])}
                </span>
            </div>
            <Slider
                value={value}
                onValueChange={(v) => onChange(v as [number, number])}
                min={min}
                max={max}
                step={5000}
                className="w-full"
            />
        </div>
    );
}
