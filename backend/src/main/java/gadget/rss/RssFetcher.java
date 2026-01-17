package gadget.rss;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import gadget.model.Gadget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * RSSフィードからガジェット情報を取得するクラス
 */
public class RssFetcher {
    private static final Logger logger = LoggerFactory.getLogger(RssFetcher.class);

    // テック系RSSフィード一覧
    private static final List<FeedSource> FEED_SOURCES = List.of(
            new FeedSource("ITmedia Mobile", "https://rss.itmedia.co.jp/rss/2.0/mobile.xml"),
            new FeedSource("ITmedia PC USER", "https://rss.itmedia.co.jp/rss/2.0/pcuser.xml"),
            new FeedSource("CNET Japan", "http://feeds.japan.cnet.com/rss/cnet/all.rdf"),
            new FeedSource("Impress Watch", "https://www.watch.impress.co.jp/data/rss/1.0/ipw/feed.rdf"),
            new FeedSource("PC Watch", "https://pc.watch.impress.co.jp/data/rss/1.0/pcw/feed.rdf"),
            new FeedSource("AV Watch", "https://av.watch.impress.co.jp/data/rss/1.0/avw/feed.rdf"),
            new FeedSource("ケータイ Watch", "https://k-tai.watch.impress.co.jp/data/rss/1.0/ktw/feed.rdf"));

    /**
     * 全てのRSSフィードからガジェット情報を取得
     */
    public List<Gadget> fetchAll() {
        List<Gadget> allGadgets = new ArrayList<>();

        for (FeedSource source : FEED_SOURCES) {
            try {
                List<Gadget> gadgets = fetchFromFeed(source);
                allGadgets.addAll(gadgets);
                logger.info("Fetched {} items from {}", gadgets.size(), source.name());
            } catch (Exception e) {
                logger.warn("Failed to fetch from {}: {}", source.name(), e.getMessage());
            }
        }

        return allGadgets;
    }

    /**
     * 単一のRSSフィードからガジェット情報を取得
     */
    private List<Gadget> fetchFromFeed(FeedSource source) throws Exception {
        List<Gadget> gadgets = new ArrayList<>();

        URL feedUrl = URI.create(source.url()).toURL();
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedUrl));

        for (SyndEntry entry : feed.getEntries()) {
            // ガジェット関連の記事のみ抽出（キーワードフィルタリング）
            String title = entry.getTitle();
            if (!isGadgetRelated(title)) {
                continue;
            }

            Gadget gadget = new Gadget();
            gadget.setId(UUID.randomUUID().toString().substring(0, 8));
            gadget.setTitle(title);
            gadget.setSourceUrl(entry.getLink());
            gadget.setSourceName(source.name());

            // 公開日時
            if (entry.getPublishedDate() != null) {
                gadget.setPublishedAt(entry.getPublishedDate().toInstant().toString());
            } else {
                gadget.setPublishedAt(Instant.now().toString());
            }

            // 説明文（後でAIで要約される）
            if (entry.getDescription() != null) {
                gadget.setOriginalContent(entry.getDescription().getValue());
            }

            gadgets.add(gadget);
        }

        return gadgets;
    }

    /**
     * タイトルがガジェット関連かどうかを判定
     */
    private boolean isGadgetRelated(String title) {
        if (title == null)
            return false;

        String lowerTitle = title.toLowerCase();
        String[] keywords = {
                "iphone", "android", "スマホ", "スマートフォン", "pixel", "galaxy", "xperia",
                "macbook", "surface", "ノートpc", "パソコン", "pc", "laptop",
                "apple watch", "galaxy watch", "fitbit", "ウェアラブル", "スマートウォッチ",
                "airpods", "イヤホン", "ヘッドホン", "スピーカー", "オーディオ", "sony wh", "bose",
                "alexa", "google home", "スマートホーム", "スマート家電", "iot", "nest",
                "タブレット", "ipad", "新製品", "発売", "発表", "レビュー"
        };

        for (String keyword : keywords) {
            if (lowerTitle.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * RSSフィードソース情報
     */
    private record FeedSource(String name, String url) {
    }
}
