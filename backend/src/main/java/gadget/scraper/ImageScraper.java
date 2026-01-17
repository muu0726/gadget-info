package gadget.scraper;

import gadget.model.Gadget;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Webページから画像URLを取得するスクレイパー
 */
public class ImageScraper {
    private static final Logger logger = LoggerFactory.getLogger(ImageScraper.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    /**
     * 記事URLからOGP画像またはメイン画像を取得
     */
    public String fetchImageUrl(String articleUrl) {
        try {
            Document doc = Jsoup.connect(articleUrl)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();

            // OGP画像を優先
            Element ogImage = doc.selectFirst("meta[property=og:image]");
            if (ogImage != null) {
                String content = ogImage.attr("content");
                if (!content.isEmpty()) {
                    return normalizeUrl(content, articleUrl);
                }
            }

            // Twitter Card画像
            Element twitterImage = doc.selectFirst("meta[name=twitter:image]");
            if (twitterImage != null) {
                String content = twitterImage.attr("content");
                if (!content.isEmpty()) {
                    return normalizeUrl(content, articleUrl);
                }
            }

            // 記事内の最初の大きな画像
            Elements images = doc.select("article img, .article img, .content img, main img");
            for (Element img : images) {
                String src = img.attr("src");
                if (isValidImageUrl(src)) {
                    return normalizeUrl(src, articleUrl);
                }
            }

            return null;
        } catch (Exception e) {
            logger.warn("Failed to fetch image from {}: {}", articleUrl, e.getMessage());
            return null;
        }
    }

    /**
     * 複数のガジェットに画像URLを設定
     */
    public void fetchImagesForGadgets(List<Gadget> gadgets) {
        logger.info("Fetching images for {} gadgets...", gadgets.size());
        int count = 0;
        for (Gadget gadget : gadgets) {
            if (gadget.getImageUrl() == null && gadget.getSourceUrl() != null) {
                String imageUrl = fetchImageUrl(gadget.getSourceUrl());
                if (imageUrl != null) {
                    gadget.setImageUrl(imageUrl);
                } else {
                    // フォールバック画像（Unsplash）
                    gadget.setImageUrl(getPlaceholderImage(gadget.getCategory()));
                }
            }
            count++;
            if (count % 10 == 0) {
                logger.info("Fetched images for {}/{} gadgets", count, gadgets.size());
            }

            // レート制限対策
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String normalizeUrl(String url, String baseUrl) {
        if (url.startsWith("//")) {
            return "https:" + url;
        }
        if (url.startsWith("/")) {
            try {
                java.net.URL base = new java.net.URL(baseUrl);
                return base.getProtocol() + "://" + base.getHost() + url;
            } catch (Exception e) {
                return url;
            }
        }
        return url;
    }

    private boolean isValidImageUrl(String url) {
        if (url == null || url.isEmpty())
            return false;
        String lower = url.toLowerCase();
        // 小さなアイコンやトラッキングピクセルを除外
        if (lower.contains("icon") || lower.contains("logo") || lower.contains("pixel") || lower.contains("1x1")) {
            return false;
        }
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                lower.endsWith(".png") || lower.endsWith(".webp") ||
                lower.contains("image");
    }

    /**
     * カテゴリ別のプレースホルダー画像
     */
    private String getPlaceholderImage(String category) {
        if (category == null) {
            return "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=400&h=300&fit=crop";
        }
        return switch (category) {
            case "Mobile" -> "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=400&h=300&fit=crop";
            case "PC" -> "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=300&fit=crop";
            case "Wearable" -> "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=400&h=300&fit=crop";
            case "Audio" -> "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=300&fit=crop";
            case "Smart Home" -> "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=300&fit=crop";
            default -> "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=400&h=300&fit=crop";
        };
    }
}
