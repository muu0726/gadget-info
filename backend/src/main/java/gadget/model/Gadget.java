package gadget.model;

import java.time.Instant;

/**
 * ガジェット情報のデータモデル
 */
public class Gadget {
    private String id;
    private String title;
    private String summary;
    private Long price;
    private String priceText;
    private String category;
    private String imageUrl;
    private String sourceUrl;
    private String sourceName;
    private String publishedAt;
    private boolean isTrending;
    private String originalContent;

    public Gadget() {}

    public Gadget(String id, String title, String sourceUrl, String sourceName) {
        this.id = id;
        this.title = title;
        this.sourceUrl = sourceUrl;
        this.sourceName = sourceName;
        this.publishedAt = Instant.now().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public String getPriceText() { return priceText; }
    public void setPriceText(String priceText) { this.priceText = priceText; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public boolean isTrending() { return isTrending; }
    public void setTrending(boolean trending) { isTrending = trending; }

    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }

    @Override
    public String toString() {
        return "Gadget{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                '}';
    }
}
