import { GadgetData } from '@/types/gadget';
import { mockData } from './mock-data';

const BASE_PATH = process.env.NEXT_PUBLIC_BASE_PATH || '';
const DATA_URL = `${BASE_PATH}/data/gadgets.json`;

export async function loadGadgetData(): Promise<GadgetData> {
    try {
        // キャッシュを回避するためにタイムスタンプを付与するか、next: { revalidate: 0 } を使う
        // Static exportの場合はfetchのcache controlは効かないことがあるため、基本は静的ファイル取得
        const response = await fetch(DATA_URL, { cache: 'no-store' });

        if (!response.ok) {
            console.warn(`Failed to fetch gadgets.json (status: ${response.status}). Using mock data.`);
            return mockData;
        }

        const data: GadgetData = await response.json();

        if (!data || !Array.isArray(data.gadgets)) {
            console.warn('Invalid data format in gadgets.json. Using mock data.');
            return mockData;
        }

        console.log(`Successfully loaded ${data.gadgets.length} items from ${DATA_URL}`);
        return data;
    } catch (error) {
        console.error('Error loading gadgets.json:', error);
        return mockData;
    }
}
