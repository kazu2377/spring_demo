package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tests")
public class Test {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String testName;
    
    @Column
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;
    
    @Column(nullable = false)
    private LocalDateTime testDate;
    
    @Column
    private LocalTime testTime;
    
    @Column(nullable = false)
    private Integer maxScore;
    
    @Column(nullable = false)
    private Integer passingScore;
    
    @Column
    private Integer duration; // テスト制限時間（分）
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestType testType;
    
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestScore> testScores = new ArrayList<>();
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    public Test() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Test(String testName, SchoolClass schoolClass, LocalDateTime testDate, Integer maxScore, Integer passingScore, TestType testType) {
        this();
        this.testName = testName;
        this.schoolClass = schoolClass;
        this.testDate = testDate;
        this.maxScore = maxScore;
        this.passingScore = passingScore;
        this.testType = testType;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTestName() {
        return testName;
    }
    
    public void setTestName(String testName) {
        this.testName = testName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public SchoolClass getSchoolClass() {
        return schoolClass;
    }
    
    public void setSchoolClass(SchoolClass schoolClass) {
        this.schoolClass = schoolClass;
    }
    
    public LocalDateTime getTestDate() {
        return testDate;
    }
    
    public void setTestDate(LocalDateTime testDate) {
        this.testDate = testDate;
    }
    
    public void setTestDate(LocalDate testDate) {
        if (testDate != null) {
            if (this.testTime != null) {
                this.testDate = testDate.atTime(this.testTime);
            } else {
                this.testDate = testDate.atStartOfDay();
            }
        }
    }
    
    public LocalTime getTestTime() {
        return testTime;
    }
    
    public void setTestTime(LocalTime testTime) {
        this.testTime = testTime;
        if (this.testDate != null && testTime != null) {
            this.testDate = this.testDate.toLocalDate().atTime(testTime);
        }
    }
    
    public Integer getMaxScore() {
        return maxScore;
    }
    
    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }
    
    public Integer getPassingScore() {
        return passingScore;
    }
    
    public void setPassingScore(Integer passingScore) {
        this.passingScore = passingScore;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public TestType getTestType() {
        return testType;
    }
    
    public void setTestType(TestType testType) {
        this.testType = testType;
    }
    
    public List<TestScore> getTestScores() {
        return testScores;
    }
    
    public void setTestScores(List<TestScore> testScores) {
        this.testScores = testScores;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public double getPassingRate() {
        if (testScores == null || testScores.isEmpty()) {
            return 0.0;
        }
        long passedCount = testScores.stream()
            .filter(score -> score.getScore() >= passingScore)
            .count();
        return (double) passedCount / testScores.size() * 100;
    }
    
    public double getAverageScore() {
        if (testScores == null || testScores.isEmpty()) {
            return 0.0;
        }
        return testScores.stream()
            .mapToInt(TestScore::getScore)
            .average()
            .orElse(0.0);
    }
}