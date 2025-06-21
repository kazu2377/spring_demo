package com.example.demo.repository;

import com.example.demo.entity.TestScore;
import com.example.demo.entity.Student;
import com.example.demo.entity.Test;
import com.example.demo.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}