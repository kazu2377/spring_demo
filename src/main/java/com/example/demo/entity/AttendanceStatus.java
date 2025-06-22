package com.example.demo.entity;

public enum AttendanceStatus {
    PRESENT("出席"),
    ABSENT("欠席"),
    LATE("遅刻"),
    EARLY_LEAVE("早退"),
    EXCUSED("公欠");
    
    private final String displayName;
    
    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}