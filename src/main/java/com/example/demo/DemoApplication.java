package com.example.demo;

import com.example.demo.service.DataInitializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private DataInitializationService dataInitializationService;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // サンプルデータの初期化を実行
        dataInitializationService.initializeSampleData();
        System.out.println("サンプルデータの初期化が完了しました。");
        System.out.println("ログイン情報:");
        System.out.println("  管理者: admin / admin123");
        System.out.println("  講師: teacher1 / teacher123");
        System.out.println("  受講生: yamada / student123");
    }
}