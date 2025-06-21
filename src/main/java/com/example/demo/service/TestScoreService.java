package com.example.demo.service;

import com.example.demo.entity.TestScore;
import com.example.demo.entity.Student;
import com.example.demo.entity.Test;
import com.example.demo.entity.SchoolClass;
import com.example.demo.entity.User;
import com.example.demo.repository.TestScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TestScoreService {
    
    @Autowired
    private TestScoreRepository testScoreRepository;
    
    public List<TestScore> getAllTestScores() {
        return testScoreRepository.findAll();
    }
    
    public Optional<TestScore> getTestScoreById(Long id) {
        return testScoreRepository.findById(id);
    }
    
    public Optional<TestScore> getTestScoreByStudentAndTest(Student student, Test test) {
        return testScoreRepository.findByStudentAndTest(student, test);
    }
    
    public List<TestScore> getTestScoresByStudent(Student student) {
        return testScoreRepository.findByStudentOrderByTestTestDateDesc(student);
    }
    
    public List<TestScore> getTestScoresByTest(Test test) {
        return testScoreRepository.findByTestOrderByScoreDesc(test);
    }
    
    public List<TestScore> getStudentScoresInClass(Long studentId, Long classId) {
        return testScoreRepository.findStudentScoresInClass(studentId, classId);
    }
    
    public List<TestScore> getScoresByClass(SchoolClass schoolClass) {
        return testScoreRepository.findBySchoolClassOrderByTestDate(schoolClass);
    }
    
    public List<TestScore> getHighScoresInClass(Long classId, Integer minScore) {
        return testScoreRepository.findHighScoresInClass(classId, minScore);
    }
    
    public TestScore recordScore(Student student, Test test, Integer score, String feedback, User gradedBy) {
        if (score < 0 || score > test.getMaxScore()) {
            throw new RuntimeException("点数は0以上、満点以下である必要があります");
        }
        
        Optional<TestScore> existingScore = testScoreRepository.findByStudentAndTest(student, test);
        
        if (existingScore.isPresent()) {
            TestScore testScore = existingScore.get();
            testScore.setScore(score);
            testScore.setFeedback(feedback);
            testScore.setGradedAt(LocalDateTime.now());
            testScore.setGradedBy(gradedBy);
            return testScoreRepository.save(testScore);
        } else {
            TestScore testScore = new TestScore(student, test, score);
            testScore.setFeedback(feedback);
            testScore.setGradedAt(LocalDateTime.now());
            testScore.setGradedBy(gradedBy);
            return testScoreRepository.save(testScore);
        }
    }
    
    public TestScore updateScore(Long id, Integer score, String feedback, User gradedBy) {
        return testScoreRepository.findById(id)
            .map(testScore -> {
                if (score < 0 || score > testScore.getTest().getMaxScore()) {
                    throw new RuntimeException("点数は0以上、満点以下である必要があります");
                }
                
                testScore.setScore(score);
                testScore.setFeedback(feedback);
                testScore.setGradedAt(LocalDateTime.now());
                testScore.setGradedBy(gradedBy);
                return testScoreRepository.save(testScore);
            })
            .orElseThrow(() -> new RuntimeException("テスト成績が見つかりません: " + id));
    }
    
    public void deleteScore(Long id) {
        testScoreRepository.deleteById(id);
    }
    
    public Double getAverageScoreByStudent(Student student) {
        return testScoreRepository.findAverageScoreByStudent(student);
    }
    
    public Double getAverageScoreByTest(Test test) {
        return testScoreRepository.findAverageScoreByTest(test);
    }
    
    public long getPassingScoreCountByTest(Test test) {
        return testScoreRepository.countPassingScoresByTest(test, test.getPassingScore());
    }
    
    public long getPassingScoreCountByStudent(Student student) {
        return testScoreRepository.countPassingScoresByStudent(student);
    }
    
    public Integer getMaxScoreByTest(Test test) {
        return testScoreRepository.findMaxScoreByTest(test);
    }
    
    public Integer getMinScoreByTest(Test test) {
        return testScoreRepository.findMinScoreByTest(test);
    }
    
    public boolean hasScore(Student student, Test test) {
        return testScoreRepository.existsByStudentAndTest(student, test);
    }
    
    public TestStatistics getTestStatistics(Test test) {
        List<TestScore> scores = testScoreRepository.findByTest(test);
        
        if (scores.isEmpty()) {
            return new TestStatistics(0, 0.0, 0, 0, 0.0, 0);
        }
        
        int totalScores = scores.size();
        double averageScore = getAverageScoreByTest(test);
        int maxScore = getMaxScoreByTest(test);
        int minScore = getMinScoreByTest(test);
        double passingRate = test.getPassingRate();
        long passedCount = getPassingScoreCountByTest(test);
        
        return new TestStatistics(totalScores, averageScore, maxScore, minScore, passingRate, passedCount);
    }
    
    public StudentGradeReport getStudentGradeReport(Student student) {
        List<TestScore> scores = getTestScoresByStudent(student);
        
        if (scores.isEmpty()) {
            return new StudentGradeReport(0, 0.0, 0, 0.0);
        }
        
        int totalTests = scores.size();
        Double averageScore = getAverageScoreByStudent(student);
        long passedTests = getPassingScoreCountByStudent(student);
        double passingRate = (double) passedTests / totalTests * 100;
        
        return new StudentGradeReport(totalTests, averageScore != null ? averageScore : 0.0, passedTests, passingRate);
    }
    
    public static class TestStatistics {
        private final int totalScores;
        private final double averageScore;
        private final int maxScore;
        private final int minScore;
        private final double passingRate;
        private final long passedCount;
        
        public TestStatistics(int totalScores, double averageScore, int maxScore, int minScore, double passingRate, long passedCount) {
            this.totalScores = totalScores;
            this.averageScore = averageScore;
            this.maxScore = maxScore;
            this.minScore = minScore;
            this.passingRate = passingRate;
            this.passedCount = passedCount;
        }
        
        public int getTotalScores() { return totalScores; }
        public double getAverageScore() { return averageScore; }
        public int getMaxScore() { return maxScore; }
        public int getMinScore() { return minScore; }
        public double getPassingRate() { return passingRate; }
        public long getPassedCount() { return passedCount; }
    }
    
    public static class StudentGradeReport {
        private final int totalTests;
        private final double averageScore;
        private final long passedTests;
        private final double passingRate;
        
        public StudentGradeReport(int totalTests, double averageScore, long passedTests, double passingRate) {
            this.totalTests = totalTests;
            this.averageScore = averageScore;
            this.passedTests = passedTests;
            this.passingRate = passingRate;
        }
        
        public int getTotalTests() { return totalTests; }
        public double getAverageScore() { return averageScore; }
        public long getPassedTests() { return passedTests; }
        public double getPassingRate() { return passingRate; }
    }
}