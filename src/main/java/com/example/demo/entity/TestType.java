package com.example.demo.entity;

public enum TestType {
    MIDTERM("中間テスト"),
    FINAL("期末テスト"),
    QUIZ("小テスト"),
    ASSIGNMENT("課題"),
    PRACTICAL("実技テスト"),
    OTHER("その他");
    
    private final String displayName;
    
    TestType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}