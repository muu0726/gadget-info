import type { GadgetData } from '@/types/gadget';

/**
 * ガジェットデータを取得する
 * 本番環境ではJSONファイルから、開発環境ではモックデータをフォールバック
 */
export async function getGadgetData(): Promise<GadgetData> {
    try {
        // 静的JSONファイルから読み込み
        const response = await fetch('/data/gadgets.json');
        if (response.ok) {
            return await response.json();
        }
    } catch {
        console.log('Using mock data (JSON not available)');
    }

    // フォールバック: モックデータを使用
    const { mockData } = await import('@/data/mock-data');
    return mockData;
}

/**
 * ビルド時にデータを読み込む（SSG用）
 */
export function getGadgetDataSync(): GadgetData {
    // SSGビルド時はモックデータを使用
    // 実際のデータはGitHub Actionsで生成されpublic/dataに配置される
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const mockData = require('@/data/mock-data').mockData;
    return mockData;
}
