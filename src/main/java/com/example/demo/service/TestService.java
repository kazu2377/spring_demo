package com.example.demo.service;

import com.example.demo.entity.Test;
import com.example.demo.entity.SchoolClass;
import com.example.demo.entity.User;
import com.example.demo.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TestService {
    
    @Autowired
    private TestRepository testRepository;
    
    public List<Test> getAllTests() {
        return testRepository.findAll();
    }
    
    public List<Test> getActiveTests() {
        return testRepository.findByActiveTrue();
    }
    
    public Optional<Test> getTestById(Long id) {
        return testRepository.findById(id);
    }
    
    public List<Test> getTestsByClass(SchoolClass schoolClass) {
        return testRepository.findBySchoolClassAndActiveTrue(schoolClass);
    }
    
    public List<Test> getTestsByType(Test.TestType testType) {
        return testRepository.findByTestType(testType);
    }
    
    public List<Test> getTestsByClassAndType(SchoolClass schoolClass, Test.TestType testType) {
        return testRepository.findBySchoolClassAndTestType(schoolClass, testType);
    }
    
    public List<Test> getTestsInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return testRepository.findTestsInPeriod(startDate, endDate);
    }
    
    public List<Test> getClassTestsInPeriod(Long classId, LocalDateTime startDate, LocalDateTime endDate) {
        return testRepository.findClassTestsInPeriod(classId, startDate, endDate);
    }
    
    public List<Test> getUpcomingTests() {
        return testRepository.findUpcomingTests(LocalDateTime.now());
    }
    
    public List<Test> getUpcomingTestsForClass(Long classId) {
        return testRepository.findUpcomingTestsForClass(classId, LocalDateTime.now());
    }
    
    public List<Test> searchTestsByName(String name) {
        return testRepository.findByTestNameContaining(name);
    }
    
    public Test createTest(Test test, User createdBy) {
        if (test.getMaxScore() <= 0) {
            throw new RuntimeException("満点は0より大きい値である必要があります");
        }
        
        if (test.getPassingScore() < 0 || test.getPassingScore() > test.getMaxScore()) {
            throw new RuntimeException("合格点は0以上、満点以下である必要があります");
        }
        
        if (test.getTestDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("テスト日時は現在時刻より後である必要があります");
        }
        
        test.setCreatedBy(createdBy);
        return testRepository.save(test);
    }
    
    public Test updateTest(Long id, Test updatedTest) {
        return testRepository.findById(id)
            .map(test -> {
                test.setTestName(updatedTest.getTestName());
                test.setDescription(updatedTest.getDescription());
                test.setTestDate(updatedTest.getTestDate());
                test.setMaxScore(updatedTest.getMaxScore());
                test.setPassingScore(updatedTest.getPassingScore());
                test.setTestType(updatedTest.getTestType());
                
                if (test.getMaxScore() <= 0) {
                    throw new RuntimeException("満点は0より大きい値である必要があります");
                }
                
                if (test.getPassingScore() < 0 || test.getPassingScore() > test.getMaxScore()) {
                    throw new RuntimeException("合格点は0以上、満点以下である必要があります");
                }
                
                return testRepository.save(test);
            })
            .orElseThrow(() -> new RuntimeException("テストが見つかりません: " + id));
    }
    
    public void deactivateTest(Long id) {
        testRepository.findById(id)
            .ifPresentOrElse(
                test -> {
                    test.setActive(false);
                    testRepository.save(test);
                },
                () -> {
                    throw new RuntimeException("テストが見つかりません: " + id);
                }
            );
    }
    
    public void reactivateTest(Long id) {
        testRepository.findById(id)
            .ifPresentOrElse(
                test -> {
                    test.setActive(true);
                    testRepository.save(test);
                },
                () -> {
                    throw new RuntimeException("テストが見つかりません: " + id);
                }
            );
    }
    
    public long getTestCountByClass(SchoolClass schoolClass) {
        return testRepository.countBySchoolClassAndActiveTrue(schoolClass);
    }
    
    public boolean isTestInFuture(Long testId) {
        return testRepository.findById(testId)
            .map(test -> test.getTestDate().isAfter(LocalDateTime.now()))
            .orElse(false);
    }
    
    public boolean isTestInPast(Long testId) {
        return testRepository.findById(testId)
            .map(test -> test.getTestDate().isBefore(LocalDateTime.now()))
            .orElse(false);
    }
    
    public List<Test> getTodaysTests() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return testRepository.findTestsInPeriod(startOfDay, endOfDay);
    }
    
    public List<Test> getThisWeeksTests() {
        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1)
                                                      .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfWeek = startOfWeek.plusDays(6).withHour(23).withMinute(59).withSecond(59);
        return testRepository.findTestsInPeriod(startOfWeek, endOfWeek);
    }
}