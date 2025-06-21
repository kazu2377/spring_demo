package com.example.demo.repository;

import com.example.demo.entity.SchoolClass;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    
    Optional<SchoolClass> findByClassName(String className);
    
    List<SchoolClass> findByTeacher(User teacher);
    
    List<SchoolClass> findByActiveTrue();
    
    List<SchoolClass> findByTeacherAndActiveTrue(User teacher);
    
    List<SchoolClass> findBySubject(String subject);
    
    @Query("SELECT c FROM SchoolClass c WHERE c.startDate <= :date AND c.endDate >= :date AND c.active = true")
    List<SchoolClass> findActiveClassesOnDate(@Param("date") LocalDate date);
    
    @Query("SELECT c FROM SchoolClass c WHERE c.active = true AND c.startDate <= CURRENT_DATE AND c.endDate >= CURRENT_DATE")
    List<SchoolClass> findCurrentActiveClasses();
    
    @Query("SELECT c FROM SchoolClass c WHERE LOWER(c.className) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(c.subject) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<SchoolClass> findByClassNameOrSubjectContaining(@Param("name") String name);
    
    boolean existsByClassName(String className);
    
    long countByActiveTrue();
    
    long countByTeacherAndActiveTrue(User teacher);
}