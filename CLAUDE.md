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

## 職業訓練校管理システム実装計画

現在、基本的な認証システムから職業訓練校の受講生管理システムへの拡張を進行中。

### 実装タスク（優先度順）

#### 高優先度 - データモデル構築
1. Roleエンティティを拡張してTEACHER、STUDENTロールを追加
2. Studentエンティティを作成（受講生情報）
3. Classエンティティを作成（クラス情報）
4. Attendanceエンティティを作成（出欠席情報）
5. Testエンティティを作成（テスト情報）
6. TestScoreエンティティを作成（テスト成績情報）

#### 中優先度 - ビジネスロジック・API層
7. 各エンティティのRepositoryインターフェースを作成
8. 各エンティティのServiceクラスを作成
9. 受講生管理用のControllerを作成
10. 出欠席管理用のControllerを作成
11. テスト・成績管理用のControllerを作成
12. SecurityConfigを更新して新しいロールとアクセス制御を設定

#### 低優先度 - UI層
13. 管理者用テンプレート（受講生一覧、出欠席入力等）を作成
14. 受講生用テンプレート（個人ダッシュボード、成績確認）を作成

### 想定される画面構成

#### 管理者（講師）用画面
- 受講生管理画面: 受講生の登録・編集・削除
- クラス管理画面: クラス/コースの作成・管理
- 出欠席管理画面: 日次出欠席の入力・確認
- テスト管理画面: テストの作成・実施・採点
- 成績一覧画面: 受講生別・クラス別の成績表示
- 出席率レポート画面: 統計情報とレポート生成

#### 受講生用画面
- 個人ダッシュボード: 自分の出席状況・成績確認
- スケジュール画面: 授業予定・テスト予定の確認
- 成績詳細画面: 自分のテスト結果・成績推移
