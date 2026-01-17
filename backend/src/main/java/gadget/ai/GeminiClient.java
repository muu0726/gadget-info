package gadget.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gadget.model.Gadget;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gemini APIを使用してガジェット情報を加工するクラス
 */
public class GeminiClient {
    private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);
    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public GeminiClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * ガジェット情報をAIで加工（要約・価格抽出・カテゴリ判定）
     */
    public void processGadget(Gadget gadget) {
        try {
            String prompt = buildPrompt(gadget);
            String response = callGeminiApi(prompt);
            parseAndApplyResponse(gadget, response);

            // レート制限対策（1秒待機）
            Thread.sleep(1000);
        } catch (Exception e) {
            logger.warn("Failed to process gadget {}: {}", gadget.getTitle(), e.getMessage());
            // デフォルト値を設定
            setDefaultValues(gadget);
        }
    }

    /**
     * バッチで複数のガジェットを処理
     */
    public void processGadgets(List<Gadget> gadgets) {
        logger.info("Processing {} gadgets with Gemini AI...", gadgets.size());
        int count = 0;
        for (Gadget gadget : gadgets) {
            processGadget(gadget);
            count++;
            if (count % 10 == 0) {
                logger.info("Processed {}/{} gadgets", count, gadgets.size());
            }
        }
        logger.info("Completed processing {} gadgets", gadgets.size());
    }

    private String buildPrompt(Gadget gadget) {
        return """
                以下のガジェット情報を分析して、JSON形式で回答してください。

                タイトル: %s
                内容: %s

                回答形式（JSON）:
                {
                  "summary": "3行以内の日本語要約（製品の特徴、性能、価格などの要点）",
                  "price": 税込価格（数値のみ、不明な場合はnull）,
                  "priceText": "価格表示テキスト（例：¥99,800、不明な場合は「価格未定」）",
                  "category": "カテゴリ（Mobile/PC/Wearable/Audio/Smart Home のいずれか）",
                  "isTrending": トレンド性が高いかどうか（true/false）
                }

                注意:
                - summaryは必ず日本語で、製品の魅力が伝わる文章にしてください
                - categoryは必ず5つのうちいずれかを選択してください
                - isTrendingは、新製品発表や大きなアップデートの場合にtrueにしてください
                """.formatted(
                gadget.getTitle(),
                gadget.getOriginalContent() != null ? gadget.getOriginalContent() : "（内容なし）");
    }

    private String callGeminiApi(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();

        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        Request request = new Request.Builder()
                .url(API_BASE_URL + "?key=" + apiKey)
                .post(RequestBody.create(
                        gson.toJson(requestBody),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API request failed: " + response.code());
            }
            return response.body().string();
        }
    }

    private void parseAndApplyResponse(Gadget gadget, String response) {
        try {
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                setDefaultValues(gadget);
                return;
            }

            JsonObject candidate = candidates.get(0).getAsJsonObject();
            JsonObject content = candidate.getAsJsonObject("content");
            JsonArray parts = content.getAsJsonArray("parts");
            String text = parts.get(0).getAsJsonObject().get("text").getAsString();

            // JSONを抽出
            String jsonStr = extractJson(text);
            JsonObject aiResult = gson.fromJson(jsonStr, JsonObject.class);

            // 結果を適用
            if (aiResult.has("summary") && !aiResult.get("summary").isJsonNull()) {
                gadget.setSummary(aiResult.get("summary").getAsString());
            }
            if (aiResult.has("price") && !aiResult.get("price").isJsonNull()) {
                gadget.setPrice(aiResult.get("price").getAsLong());
            }
            if (aiResult.has("priceText") && !aiResult.get("priceText").isJsonNull()) {
                gadget.setPriceText(aiResult.get("priceText").getAsString());
            } else if (gadget.getPrice() != null) {
                gadget.setPriceText("¥" + String.format("%,d", gadget.getPrice()));
            } else {
                gadget.setPriceText("価格未定");
            }
            if (aiResult.has("category") && !aiResult.get("category").isJsonNull()) {
                String category = aiResult.get("category").getAsString();
                if (isValidCategory(category)) {
                    gadget.setCategory(category);
                } else {
                    gadget.setCategory(guessCategory(gadget.getTitle()));
                }
            }
            if (aiResult.has("isTrending") && !aiResult.get("isTrending").isJsonNull()) {
                gadget.setTrending(aiResult.get("isTrending").getAsBoolean());
            }

        } catch (Exception e) {
            logger.warn("Failed to parse AI response: {}", e.getMessage());
            setDefaultValues(gadget);
        }
    }

    private String extractJson(String text) {
        // ```json ... ``` で囲まれたJSON、または { } で囲まれたJSONを抽出
        Pattern codeBlockPattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");
        Matcher codeBlockMatcher = codeBlockPattern.matcher(text);
        if (codeBlockMatcher.find()) {
            return codeBlockMatcher.group(1).trim();
        }

        Pattern jsonPattern = Pattern.compile("\\{[\\s\\S]*\\}");
        Matcher jsonMatcher = jsonPattern.matcher(text);
        if (jsonMatcher.find()) {
            return jsonMatcher.group().trim();
        }

        return text;
    }

    private boolean isValidCategory(String category) {
        return category != null && List.of("Mobile", "PC", "Wearable", "Audio", "Smart Home").contains(category);
    }

    private String guessCategory(String title) {
        if (title == null)
            return "Mobile";
        String lower = title.toLowerCase();

        if (lower.contains("iphone") || lower.contains("android") || lower.contains("スマホ") || lower.contains("galaxy")
                || lower.contains("pixel")) {
            return "Mobile";
        }
        if (lower.contains("macbook") || lower.contains("pc") || lower.contains("パソコン") || lower.contains("laptop")
                || lower.contains("surface")) {
            return "PC";
        }
        if (lower.contains("watch") || lower.contains("ウェアラブル") || lower.contains("fitbit") || lower.contains("リング")) {
            return "Wearable";
        }
        if (lower.contains("airpods") || lower.contains("イヤホン") || lower.contains("ヘッドホン") || lower.contains("スピーカー")
                || lower.contains("オーディオ")) {
            return "Audio";
        }
        if (lower.contains("alexa") || lower.contains("google home") || lower.contains("スマートホーム")
                || lower.contains("nest")) {
            return "Smart Home";
        }

        return "Mobile"; // デフォルト
    }

    private void setDefaultValues(Gadget gadget) {
        if (gadget.getSummary() == null) {
            gadget.setSummary(gadget.getTitle() + "に関する最新情報です。");
        }
        if (gadget.getPriceText() == null) {
            gadget.setPriceText("価格未定");
        }
        if (gadget.getCategory() == null) {
            gadget.setCategory(guessCategory(gadget.getTitle()));
        }
    }
}
