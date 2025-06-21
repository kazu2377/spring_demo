# CLAUDE.md

## 日本語で記載

このファイルは、Claude Code (claude.ai/code) がこのリポジトリのコードを扱う際のガイダンスを提供します。

## プロジェクト概要

**職業訓練校管理システム** - Spring Security を使用した認証・認可機能付きの Spring Boot MVC Web アプリケーションです。受講生の出欠席・成績管理を総合的に行える教育機関向けシステムです。開発環境では H2 インメモリデータベースを使用し、4つのロール（ADMIN/USER/TEACHER/STUDENT）によるアクセス制御をサポートしています。

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
├── config/SecurityConfig.java          # Spring Security設定（4ロール対応）
├── controller/                         # Webコントローラー
│   ├── DashboardController.java       # 役割別ダッシュボード振り分け
│   ├── StudentController.java         # 受講生管理
│   ├── SchoolClassController.java     # クラス管理
│   ├── AttendanceController.java      # 出欠席管理
│   ├── TestController.java           # テスト・成績管理
│   ├── StudentDashboardController.java # 受講生用ダッシュボード
│   ├── AuthController.java           # 認証エンドポイント
│   └── AdminController.java          # 管理者専用エンドポイント
├── entity/                           # データモデル
│   ├── User.java                     # ユーザー（4ロール対応）
│   ├── Student.java                  # 受講生情報
│   ├── SchoolClass.java              # クラス情報
│   ├── Attendance.java               # 出欠席記録
│   ├── Test.java                     # テスト情報
│   ├── TestScore.java                # 成績記録
│   └── Role.java                     # ロール定義（ADMIN/USER/TEACHER/STUDENT）
├── repository/                       # データアクセス層
│   ├── UserRepository.java
│   ├── StudentRepository.java
│   ├── SchoolClassRepository.java
│   ├── AttendanceRepository.java
│   ├── TestRepository.java
│   └── TestScoreRepository.java
├── service/                          # ビジネスロジック層
│   ├── UserService.java
│   ├── StudentService.java
│   ├── SchoolClassService.java
│   ├── AttendanceService.java
│   ├── TestService.java
│   └── TestScoreService.java
└── DemoApplication.java             # メインアプリケーションクラス

src/main/resources/
├── templates/                        # Thymeleafテンプレート
│   ├── login.html                   # ログインページ
│   ├── dashboard.html               # 従来ダッシュボード（USER用）
│   ├── school-dashboard.html        # 職業訓練校管理ダッシュボード
│   ├── students/                    # 受講生管理画面
│   │   ├── list.html               # 一覧
│   │   ├── view.html               # 詳細
│   │   ├── create.html             # 新規登録
│   │   └── edit.html               # 編集
│   ├── classes/                     # クラス管理画面
│   │   └── list.html               # 一覧
│   ├── attendance/                  # 出欠席管理画面
│   │   └── list.html               # 出欠席入力
│   ├── tests/                       # テスト管理画面
│   │   └── list.html               # 一覧
│   ├── student/                     # 受講生用画面
│   │   ├── dashboard.html          # 個人ダッシュボード
│   │   └── scores.html             # 成績確認
│   └── admin/                       # 管理者専用ページ
├── static/                          # CSS、JS、画像
└── application.properties           # アプリケーション設定
```

## セキュリティ設定

- **フォームベース認証**によるカスタムログインページ
- **4段階ロールベースアクセス制御**:
  - **ADMIN**: 全システム管理権限
  - **TEACHER**: 職業訓練校管理機能（受講生・クラス・出欠席・テスト管理）
  - **STUDENT**: 個人ダッシュボード・成績確認のみ
  - **USER**: 従来の一般ユーザー機能
- **役割別ダッシュボード自動振り分け**: ログイン後に適切なダッシュボードにリダイレクト
- **パス別アクセス制御**: 
  - `/admin/**` → ADMIN のみ
  - `/students/**`, `/classes/**`, `/attendance/**`, `/tests/**` → ADMIN、TEACHER
  - `/student/**` → ADMIN、TEACHER、STUDENT

## データベース設定

- **開発環境用 H2 インメモリデータベース**
- **エンティティ構成**:
  - **User**: ユーザー名、パスワード（暗号化済み）、ロール（4種類）
  - **Student**: 受講生情報（学籍番号、氏名、メール、入学日等）
  - **SchoolClass**: クラス情報（クラス名、科目、期間、講師、定員）
  - **Attendance**: 出欠席記録（受講生、日付、ステータス、記録者）
  - **Test**: テスト情報（テスト名、実施日時、満点、合格点、種別）
  - **TestScore**: 成績記録（受講生、テスト、点数、フィードバック）
- **リレーション**:
  - Student ↔ User (1:1)
  - Student ↔ SchoolClass (N:1)
  - SchoolClass ↔ User(Teacher) (N:1)
  - Attendance → Student, SchoolClass (N:1)
  - Test → SchoolClass (N:1)
  - TestScore → Student, Test (N:1)

## 開発上の注意点

- メソッドレベルセキュリティには `@PreAuthorize` アノテーションを使用
- ロールベースの条件付きレンダリングには Thymeleaf `sec:authorize` を使用
- セキュリティのため BCrypt パスワード暗号化を使用
- 適切なログアウト処理を伴うセッションベース認証

## 職業訓練校管理システム実装状況

### ✅ 完了済み機能（Phase 1）

#### データモデル・バックエンド（完了）
- ✅ Role（ADMIN/USER/TEACHER/STUDENT）
- ✅ Student（受講生情報）、SchoolClass（クラス情報）、Attendance（出欠席）
- ✅ Test（テスト情報）、TestScore（成績記録）
- ✅ 全Repository、Service、Controller層
- ✅ セキュリティ設定（4ロール対応、パス別アクセス制御）

#### UI・UX（部分完了）
- ✅ 役割別ダッシュボード振り分け（DashboardController）
- ✅ 職業訓練校管理ダッシュボード（school-dashboard.html）
- ✅ 受講生管理（list.html, view.html, create.html, edit.html）
- ✅ 受講生用個人ダッシュボード（dashboard.html, scores.html）
- ✅ 基本的な出欠席・テスト・クラス一覧画面

### 🔄 現在の実装計画（Phase 2-3）

#### Phase 2: 残りUI完成
- ⏳ クラス管理（create.html, edit.html, view.html）
- ⏳ テスト管理（create.html, edit.html, view.html, scores.html）
- ⏳ 出欠席詳細（student-view.html, reports.html）
- ⏳ 受講生用（attendance.html）

#### Phase 3: データ・運用機能
- ⏳ サンプルデータ作成・初期化処理
- ⏳ CSV入出力、PDF/Excel出力
- ⏳ 統計・分析機能の強化

### 画面構成（実装済み/予定）

#### 管理者（講師）用画面
- ✅ **統合ダッシュボード**: 全機能への統一アクセス
- ✅ **受講生管理**: 登録・編集・検索・クラス割当・ステータス管理
- ✅ **クラス管理**: 一覧・カード表示（作成・編集画面は実装予定）
- ✅ **出欠席管理**: 日次入力・一括処理（詳細レポート実装予定）
- ✅ **テスト管理**: 一覧・フィルタ（作成・採点画面実装予定）

#### 受講生用画面
- ✅ **個人ダッシュボード**: 成績・出席統計・進捗可視化
- ✅ **成績確認**: 詳細成績表・Chart.js推移グラフ・評価表示
- ⏳ **出席状況**: 個人出席履歴・統計（実装予定）

### 技術スタック
- **フロントエンド**: Bootstrap 5 + Font Awesome + Chart.js
- **バックエンド**: Spring Boot 3.2.0 + Spring Security + Thymeleaf
- **データベース**: H2（開発）+ JPA/Hibernate
- **認証**: BCrypt + セッション認証 + 4段階ロール制御
