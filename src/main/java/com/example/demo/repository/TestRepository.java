package com.example.demo.repository;

import com.example.demo.entity.Test;
import com.example.demo.entity.TestType;
import com.example.demo.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    
    List<Test> findBySchoolClass(SchoolClass schoolClass);
    
    List<Test> findByActiveTrue();
    
    List<Test> findBySchoolClassAndActiveTrue(SchoolClass schoolClass);
    
    List<Test> findByTestType(TestType testType);
    
    List<Test> findBySchoolClassAndTestType(SchoolClass schoolClass, TestType testType);
    
    @Query("SELECT t FROM Test t WHERE t.testDate BETWEEN :startDate AND :endDate AND t.active = true ORDER BY t.testDate")
    List<Test> findTestsInPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Test t WHERE t.schoolClass.id = :classId AND t.testDate BETWEEN :startDate AND :endDate AND t.active = true ORDER BY t.testDate")
    List<Test> findClassTestsInPeriod(@Param("classId") Long classId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Test t WHERE t.testDate >= :fromDate AND t.active = true ORDER BY t.testDate")
    List<Test> findUpcomingTests(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT t FROM Test t WHERE t.schoolClass.id = :classId AND t.testDate >= :fromDate AND t.active = true ORDER BY t.testDate")
    List<Test> findUpcomingTestsForClass(@Param("classId") Long classId, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT t FROM Test t WHERE LOWER(t.testName) LIKE LOWER(CONCAT('%', :name, '%')) AND t.active = true")
    List<Test> findByTestNameContaining(@Param("name") String name);
    
    long countBySchoolClass(SchoolClass schoolClass);
    
    long countBySchoolClassAndActiveTrue(SchoolClass schoolClass);
    
    long countByActiveTrue();
    
    List<Test> findBySchoolClassId(Long classId);
}