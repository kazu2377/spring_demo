package com.example.demo;

import com.example.demo.entity.Role;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

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
}