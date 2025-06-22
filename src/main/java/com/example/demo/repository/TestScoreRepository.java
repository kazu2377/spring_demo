package com.example.demo.repository;

import com.example.demo.entity.TestScore;
import com.example.demo.entity.Student;
import com.example.demo.entity.Test;
import com.example.demo.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestScoreRepository extends JpaRepository<TestScore, Long> {
    
    Optional<TestScore> findByStudentAndTest(Student student, Test test);
    
    List<TestScore> findByStudent(Student student);
    
    List<TestScore> findByTest(Test test);
    
    List<TestScore> findByStudentOrderByTestTestDateDesc(Student student);
    
    List<TestScore> findByTestOrderByScoreDesc(Test test);
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.test.schoolClass = :schoolClass ORDER BY ts.test.testDate DESC")
    List<TestScore> findBySchoolClassOrderByTestDate(@Param("schoolClass") SchoolClass schoolClass);
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.student.id = :studentId AND ts.test.schoolClass.id = :classId ORDER BY ts.test.testDate DESC")
    List<TestScore> findStudentScoresInClass(@Param("studentId") Long studentId, @Param("classId") Long classId);
    
    @Query("SELECT AVG(ts.score) FROM TestScore ts WHERE ts.student = :student")
    Double findAverageScoreByStudent(@Param("student") Student student);
    
    @Query("SELECT AVG(ts.score) FROM TestScore ts WHERE ts.test = :test")
    Double findAverageScoreByTest(@Param("test") Test test);
    
    @Query("SELECT COUNT(ts) FROM TestScore ts WHERE ts.test = :test AND ts.score >= :passingScore")
    long countPassingScoresByTest(@Param("test") Test test, @Param("passingScore") Integer passingScore);
    
    @Query("SELECT COUNT(ts) FROM TestScore ts WHERE ts.student = :student AND ts.score >= ts.test.passingScore")
    long countPassingScoresByStudent(@Param("student") Student student);
    
    @Query("SELECT MAX(ts.score) FROM TestScore ts WHERE ts.test = :test")
    Integer findMaxScoreByTest(@Param("test") Test test);
    
    @Query("SELECT MIN(ts.score) FROM TestScore ts WHERE ts.test = :test")
    Integer findMinScoreByTest(@Param("test") Test test);
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.test.schoolClass.id = :classId AND ts.score >= :minScore ORDER BY ts.score DESC")
    List<TestScore> findHighScoresInClass(@Param("classId") Long classId, @Param("minScore") Integer minScore);
    
    boolean existsByStudentAndTest(Student student, Test test);
    
    List<TestScore> findByStudentId(Long studentId);
    
    List<TestScore> findByTestId(Long testId);
    
    List<TestScore> findByTestIdOrderByStudentStudentNumber(Long testId);
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.recordedAt BETWEEN :startDate AND :endDate")
    List<TestScore> findByRecordedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.student.id = :studentId AND ts.recordedAt BETWEEN :startDate AND :endDate")
    List<TestScore> findByStudentIdAndRecordedAtBetween(@Param("studentId") Long studentId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ts FROM TestScore ts ORDER BY ts.test.testDate DESC, ts.student.studentNumber ASC")
    List<TestScore> findAllOrderByTestDateAndStudentNumber();
    
    // クラス基準のメソッド
    @Query("SELECT ts FROM TestScore ts WHERE ts.test.schoolClass.id = :classId")
    List<TestScore> findBySchoolClassId(@Param("classId") Long classId);
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.test.schoolClass.id = :classId ORDER BY ts.test.testDate DESC")
    List<TestScore> findBySchoolClassIdOrderByTestDate(@Param("classId") Long classId);
    
    // 期間指定の検索メソッド
    @Query("SELECT ts FROM TestScore ts WHERE ts.test.testDate BETWEEN :startDate AND :endDate")
    List<TestScore> findByTestDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.student.id = :studentId AND ts.test.testDate BETWEEN :startDate AND :endDate")
    List<TestScore> findByStudentIdAndTestDateBetween(@Param("studentId") Long studentId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 最新成績取得
    @Query("SELECT ts FROM TestScore ts WHERE ts.student.id = :studentId ORDER BY ts.recordedAt DESC")
    List<TestScore> findByStudentIdOrderByRecordedAtDesc(@Param("studentId") Long studentId);
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.test.id = :testId ORDER BY ts.score DESC")
    List<TestScore> findByTestIdOrderByScoreDesc(@Param("testId") Long testId);
    
    // 統計用メソッド
    @Query("SELECT COUNT(ts) FROM TestScore ts WHERE ts.test.schoolClass.id = :classId")
    long countBySchoolClassId(@Param("classId") Long classId);
    
    @Query("SELECT COUNT(ts) FROM TestScore ts WHERE ts.student.id = :studentId")
    long countByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(ts) FROM TestScore ts WHERE ts.test.id = :testId")
    long countByTestId(@Param("testId") Long testId);
    
    // 高得点・低得点検索
    @Query("SELECT ts FROM TestScore ts WHERE ts.score >= :minScore ORDER BY ts.score DESC")
    List<TestScore> findByScoreGreaterThanEqualOrderByScoreDesc(@Param("minScore") Integer minScore);
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.score < :maxScore ORDER BY ts.score ASC")
    List<TestScore> findByScoreLessThanOrderByScoreAsc(@Param("maxScore") Integer maxScore);
    
    // 合格・不合格検索
    @Query("SELECT ts FROM TestScore ts WHERE ts.score >= ts.test.passingScore")
    List<TestScore> findPassingScores();
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.score < ts.test.passingScore")
    List<TestScore> findFailingScores();
    
    // 学生別合格・不合格
    @Query("SELECT ts FROM TestScore ts WHERE ts.student.id = :studentId AND ts.score >= ts.test.passingScore")
    List<TestScore> findPassingScoresByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT ts FROM TestScore ts WHERE ts.student.id = :studentId AND ts.score < ts.test.passingScore")
    List<TestScore> findFailingScoresByStudentId(@Param("studentId") Long studentId);
    
    // 最新のテスト結果
    @Query("SELECT ts FROM TestScore ts WHERE ts.student.id = :studentId ORDER BY ts.test.testDate DESC LIMIT 1")
    Optional<TestScore> findLatestByStudentId(@Param("studentId") Long studentId);
    
    // 特定期間の平均点
    @Query("SELECT AVG(ts.score) FROM TestScore ts WHERE ts.test.testDate BETWEEN :startDate AND :endDate")
    Double findAverageScoreInPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // クラス別平均点
    @Query("SELECT AVG(ts.score) FROM TestScore ts WHERE ts.test.schoolClass.id = :classId")
    Double findAverageScoreByClassId(@Param("classId") Long classId);
}