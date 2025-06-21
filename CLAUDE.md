# CLAUDE.md

## 日本語で記載

このファイルは、Claude Code (claude.ai/code) がこのリポジトリのコードを扱う際のガイダンスを提供します。

## プロジェクト概要

Spring Security を使用した認証・認可機能付きの Spring Boot MVC Web アプリケーションです。開発環境では H2 インメモリデータベースを使用し、ロールベースアクセス制御（ADMIN/USER ロール）をサポートしています。

## 基本コマンド

```bash
# プロジェクトをビルド
./mvnw clean compile

# テストを実行
./mvnw test

# アプリケーションを実行
./mvnw spring-boot:run

# アプリケーションをパッケージ化
./mvnw clean package

# 特定のプロファイルで実行
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## アーキテクチャ概要

- **MVC パターン**: コントローラーが Web リクエストを処理、サービスがビジネスロジックを担当、リポジトリがデータアクセスを処理
- **セキュリティ層**: フォームベース認証とロールベース認可を使用した Spring Security
- **データベース**: JPA/Hibernate による ORM を使用した H2 インメモリデータベース
- **ビュー層**: ロールベースの条件付きレンダリングを持つ Thymeleaf テンプレート

## 主要コンポーネント構造

```
src/main/java/com/example/demo/
├── config/SecurityConfig.java          # Spring Security設定
├── controller/                         # Webコントローラー
│   ├── AuthController.java            # 認証エンドポイント
│   ├── DashboardController.java       # メインダッシュボード
│   └── AdminController.java           # 管理者専用エンドポイント
├── entity/User.java                   # ロール付きユーザーエンティティ
├── repository/UserRepository.java     # ユーザーデータアクセス
├── service/UserService.java          # ユーザービジネスロジック
└── DemoApplication.java              # メインアプリケーションクラス

src/main/resources/
├── templates/                         # Thymeleafテンプレート
│   ├── login.html                    # ログインページ
│   ├── dashboard.html                # メインダッシュボード
│   └── admin/                        # 管理者専用ページ
├── static/                           # CSS、JS、画像
└── application.properties            # アプリケーション設定
```

## セキュリティ設定

- フォームベース認証によるカスタムログインページ
- ロールベースアクセス制御：ADMIN ユーザーは全機能を表示、一般ユーザーは制限付きアクセス
- ダッシュボードはユーザーロールに基づいて異なるコンテンツを表示
- 管理者ページは ADMIN ロールアクセスが必要

## データベース設定

- 開発環境用 H2 インメモリデータベース
- ユーザーエンティティはユーザー名、パスワード（暗号化済み）、ロールを保存
- テスト用のデフォルト管理者ユーザーを起動時に作成する必要があります

## 開発上の注意点

- メソッドレベルセキュリティには `@PreAuthorize` アノテーションを使用
- ロールベースの条件付きレンダリングには Thymeleaf `sec:authorize` を使用
- セキュリティのため BCrypt パスワード暗号化を使用
- 適切なログアウト処理を伴うセッションベース認証
