# Spring Security + MVC 認証システム実装記録

## 概要

Spring Boot 3.2.0 を使用して、認証・認可機能を持つ Web アプリケーションを一から実装しました。Spring Security によるフォームベース認証と、ロールベースのアクセス制御（ADMIN/USER）を実装し、完全な管理者・一般ユーザー管理システムを構築しました。

## 技術スタック

- **Spring Boot**: 3.2.0
- **Spring Security**: 6.x
- **Spring Data JPA**: データベースアクセス
- **H2 Database**: インメモリデータベース（開発環境）
- **Thymeleaf**: テンプレートエンジン
- **BCrypt**: パスワードハッシュ化

## 実装内容

### 1. セキュリティ設定の実装

`SecurityConfig.java` では、Spring Security の設定を行いました。

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
            );
        return http.build();
    }
}
```

**重要なポイント：**
- `@EnableMethodSecurity(prePostEnabled = true)` によりメソッドレベルの認可を有効化
- `/admin/**` パスは ADMIN ロールのみアクセス可能
- BCrypt によるパスワード暗号化

### 2. ユーザーエンティティとロール管理

`User.java` で Spring Security の `UserDetails` インターフェースを実装：

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
```

**学んだこと：**
- `UserDetails` の実装により Spring Security との統合が簡単になる
- `@Enumerated(EnumType.STRING)` でロールをデータベースに文字列として保存
- 権限名には `ROLE_` プレフィックスが必要

### 3. 管理者機能の実装

`AdminController.java` でメソッドレベルの認可を活用：

```java
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @GetMapping("/users")
    public String userManagement(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }
    
    @PostMapping("/users")
    public String createUser(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam Role role,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(username, password, role);
            redirectAttributes.addFlashAttribute("successMessage", "ユーザーが正常に作成されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "ユーザーの作成に失敗しました: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
```

**実装のポイント：**
- クラスレベルでの `@PreAuthorize` により全メソッドに認可を適用
- `RedirectAttributes` でフラッシュメッセージを実装
- 例外処理によるユーザーフレンドリーなエラー表示

### 4. 初期データの自動作成

`DemoApplication.java` で `CommandLineRunner` を使用：

```java
@Override
public void run(String... args) throws Exception {
    if (!userService.getUserByUsername("admin").isPresent()) {
        userService.createUser("admin", "admin", Role.ADMIN);
        System.out.println("デフォルト管理者ユーザーを作成しました: admin/admin");
    }
    
    if (!userService.getUserByUsername("user").isPresent()) {
        userService.createUser("user", "user", Role.USER);
        System.out.println("デフォルト一般ユーザーを作成しました: user/user");
    }
}
```

### 5. Thymeleaf での条件分岐レンダリング

`dashboard.html` でロールベースの表示制御：

```html
<div th:if="${isAdmin}" class="admin-section">
    <h3>🔧 管理者メニュー</h3>
    <a th:href="@{/admin}" class="btn btn-primary">管理画面トップ</a>
    <a th:href="@{/admin/users}" class="btn btn-success">ユーザー管理</a>
</div>

<div th:unless="${isAdmin}" class="dashboard-card">
    <h3>📋 利用可能な機能</h3>
    <p>一般ユーザーとして以下の機能を利用できます:</p>
</div>
```

## 学んだこと

### 意外だった落とし穴

1. **ROLE_ プレフィックス**
   - Spring Security では権限名に `ROLE_` プレフィックスが自動的に付与される
   - `hasRole('ADMIN')` は内部的に `ROLE_ADMIN` として処理される

2. **パスワードエンコーディング**
   - 平文パスワードを保存すると認証に失敗する
   - 必ず `PasswordEncoder` を使用してハッシュ化が必要

3. **H2 コンソールアクセス**
   - CSRFトークンとX-Frame-Optionsの設定が必要
   - `.csrf().ignoringRequestMatchers("/h2-console/**")` と `.frameOptions().sameOrigin()` の設定

### 今後使えそうな知見

- **メソッドレベル認可**: `@PreAuthorize` によりきめ細かいアクセス制御が可能
- **フラッシュメッセージ**: `RedirectAttributes` でPRGパターンを実装
- **条件分岐レンダリング**: Thymeleaf の `th:if` と `th:unless` でロールベース表示

### もっと良い書き方の発見

```java
// 改善前: 例外を投げるだけ
User user = userRepository.findById(id)
    .orElseThrow(() -> new RuntimeException("User not found"));

// 改善後: 適切な例外クラスを使用
User user = userRepository.findById(id)
    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
```

## 終わりに

今回の実装により、Spring Security を使った認証・認可システムの基本的な構造を理解できました。特に、設定の統合化（Java Config）、メソッドレベルの認可、Thymeleaf との連携など、実際のプロジェクトで使える知識を得られました。

次回は、この基盤を元に JWT 認証や OAuth2 連携などのより高度な認証機能を実装してみたいと思います。