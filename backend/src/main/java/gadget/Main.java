package gadget;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gadget.ai.GeminiClient;
import gadget.model.Gadget;
import gadget.model.GadgetData;
import gadget.rss.RssFetcher;
import gadget.scraper.ImageScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ガジェット情報収集バッチのメインエントリーポイント
 * 
 * 使用方法:
 * java -jar gadget-backend.jar [GEMINI_API_KEY] [OUTPUT_DIR]
 * 
 * 環境変数:
 * GEMINI_API_KEY: Gemini APIキー
 * OUTPUT_DIR: 出力ディレクトリ（デフォルト: ../frontend/public/data）
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int MAX_GADGETS = 50; // 最大取得件数

    public static void main(String[] args) {
        logger.info("=== Gadget Info Backend ===");
        logger.info("Starting data collection...");

        // 設定の取得
        String apiKey = getConfig(args, 0, "GEMINI_API_KEY", null);
        String outputDir = getConfig(args, 1, "OUTPUT_DIR", "../frontend/public/data");

        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("GEMINI_API_KEY is not set. Running in demo mode (no AI processing).");
        }

        try {
            // 1. RSSフィードからガジェット情報を取得
            logger.info("Step 1: Fetching RSS feeds...");
            RssFetcher rssFetcher = new RssFetcher();
            List<Gadget> gadgets = rssFetcher.fetchAll();
            logger.info("Fetched {} gadgets from RSS feeds", gadgets.size());

            if (gadgets.isEmpty()) {
                logger.warn("No gadgets fetched. Check network connectivity.");
                return;
            }

            // 最新の記事を優先（最大件数制限）
            gadgets = gadgets.stream()
                    .sorted(Comparator.comparing(Gadget::getPublishedAt).reversed())
                    .limit(MAX_GADGETS)
                    .collect(Collectors.toList());
            logger.info("Limited to {} most recent gadgets", gadgets.size());

            // 2. Gemini AIで加工（APIキーがある場合のみ）
            if (apiKey != null && !apiKey.isEmpty()) {
                logger.info("Step 2: Processing with Gemini AI...");
                GeminiClient geminiClient = new GeminiClient(apiKey);
                geminiClient.processGadgets(gadgets);
            } else {
                logger.info("Step 2: Skipping AI processing (no API key)");
                // デフォルト値を設定
                for (Gadget gadget : gadgets) {
                    if (gadget.getSummary() == null) {
                        gadget.setSummary(gadget.getTitle() + "に関する最新情報です。詳細は記事をご覧ください。");
                    }
                    if (gadget.getCategory() == null) {
                        gadget.setCategory(guessCategory(gadget.getTitle()));
                    }
                    if (gadget.getPriceText() == null) {
                        gadget.setPriceText("価格未定");
                    }
                }
            }

            // 3. 画像URLを取得
            logger.info("Step 3: Fetching images...");
            ImageScraper imageScraper = new ImageScraper();
            imageScraper.fetchImagesForGadgets(gadgets);

            // 4. トレンド判定（同一製品の露出度に基づく）
            logger.info("Step 4: Calculating trends...");
            calculateTrends(gadgets);

            // 5. JSONファイルに出力
            logger.info("Step 5: Saving to JSON...");
            GadgetData gadgetData = new GadgetData(gadgets);
            saveToJson(gadgetData, outputDir);

            logger.info("=== Completed successfully! ===");
            logger.info("Output: {}/gadgets.json", outputDir);
            logger.info("Total gadgets: {}", gadgets.size());

        } catch (Exception e) {
            logger.error("Fatal error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private static String getConfig(String[] args, int index, String envName, String defaultValue) {
        // コマンドライン引数 > 環境変数 > デフォルト値
        if (args.length > index && args[index] != null && !args[index].isEmpty()) {
            return args[index];
        }
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return defaultValue;
    }

    private static String guessCategory(String title) {
        if (title == null)
            return "Mobile";
        String lower = title.toLowerCase();

        if (lower.contains("playstation") || lower.contains("ps5") || lower.contains("nintendo")
                || lower.contains("switch")
                || lower.contains("steam deck") || lower.contains("xbox") || lower.contains("geforce")
                || lower.contains("rtx") || lower.contains("gaming") || lower.contains("ゲーミング")) {
            return "Gaming";
        }
        if (lower.contains("iphone") || lower.contains("android") || lower.contains("スマホ") || lower.contains("galaxy")
                || lower.contains("pixel")) {
            return "Mobile";
        }
        if (lower.contains("macbook") || lower.contains("pc") || lower.contains("パソコン") || lower.contains("laptop")
                || lower.contains("surface")) {
            return "PC";
        }
        if (lower.contains("watch") || lower.contains("ウェアラブル") || lower.contains("fitbit")) {
            return "Wearable";
        }
        if (lower.contains("airpods") || lower.contains("イヤホン") || lower.contains("ヘッドホン") || lower.contains("スピーカー")) {
            return "Audio";
        }
        if (lower.contains("alexa") || lower.contains("google home") || lower.contains("スマートホーム")
                || lower.contains("nest")) {
            return "Smart Home";
        }

        return "Mobile";
    }

    private static void calculateTrends(List<Gadget> gadgets) {
        // 製品名のキーワードでグループ化し、複数ソースで言及されている製品をトレンドに
        java.util.Map<String, Long> keywordCounts = new java.util.HashMap<>();

        for (Gadget gadget : gadgets) {
            String title = gadget.getTitle().toLowerCase();
            // 主要な製品名キーワード
            String[] trendKeywords = { "iphone", "pixel", "galaxy", "macbook", "surface", "airpods", "apple watch" };
            for (String keyword : trendKeywords) {
                if (title.contains(keyword)) {
                    keywordCounts.merge(keyword, 1L, Long::sum);
                }
            }
        }

        // 2回以上言及された製品をトレンドに
        for (Gadget gadget : gadgets) {
            String title = gadget.getTitle().toLowerCase();
            for (java.util.Map.Entry<String, Long> entry : keywordCounts.entrySet()) {
                if (entry.getValue() >= 2 && title.contains(entry.getKey())) {
                    gadget.setTrending(true);
                    break;
                }
            }
        }

        // トレンドが少なすぎる場合、最新3件をトレンドに
        long trendCount = gadgets.stream().filter(Gadget::isTrending).count();
        if (trendCount < 3) {
            gadgets.stream()
                    .sorted(Comparator.comparing(Gadget::getPublishedAt).reversed())
                    .limit(3)
                    .forEach(g -> g.setTrending(true));
        }
    }

    private static void saveToJson(GadgetData data, String outputDir) throws IOException {
        Path dirPath = Paths.get(outputDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Path filePath = dirPath.resolve("gadgets.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            gson.toJson(data, writer);
        }

        logger.info("Saved {} gadgets to {}", data.getGadgets().size(), filePath);
    }
}
