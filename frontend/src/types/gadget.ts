export type GadgetCategory = 'Mobile' | 'PC' | 'Wearable' | 'Audio' | 'Smart Home';

export interface Gadget {
  id: string;
  title: string;
  summary: string;
  price: number | null;
  priceText: string;
  category: GadgetCategory;
  imageUrl: string;
  sourceUrl: string;
  sourceName: string;
  publishedAt: string;
  isTrending: boolean;
}

export interface GadgetData {
  gadgets: Gadget[];
  lastUpdated: string;
}
