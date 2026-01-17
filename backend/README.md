# Gadget Info Backend

ガジェット情報を収集・加工するJavaバッチアプリケーション。

## 機能

1. **RSSフィード収集**: ITmedia、Impress Watch、CNET Japan等からガジェット関連記事を取得
2. **AI加工**: Gemini APIで要約・価格抽出・カテゴリ判定
3. **画像取得**: OGP画像のスクレイピング
4. **トレンド判定**: 複数ソースで言及された製品を自動検出
5. **JSON出力**: フロントエンドで使用するデータファイルを生成

## 必要環境

- Java 21+
- Maven 3.9+

## ビルド

```bash
cd backend
mvn clean package
```

## 実行

```bash
# 環境変数で設定
export GEMINI_API_KEY="your-api-key"
java -jar target/gadget-backend-1.0.0.jar

# または引数で指定
java -jar target/gadget-backend-1.0.0.jar YOUR_API_KEY ../frontend/public/data
```

## 環境変数

| 変数 | 説明 | デフォルト |
|------|------|------------|
| GEMINI_API_KEY | Gemini APIキー | なし（デモモード） |
| OUTPUT_DIR | 出力ディレクトリ | ../frontend/public/data |

## 対応RSSフィード

- ITmedia Mobile
- ITmedia PC USER
- CNET Japan
- Impress Watch（PC Watch, AV Watch, ケータイ Watch）
