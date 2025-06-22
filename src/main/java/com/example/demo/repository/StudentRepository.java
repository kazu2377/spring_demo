package com.example.demo.repository;

import com.example.demo.entity.Student;
import com.example.demo.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    Optional<Student> findByStudentNumber(String studentNumber);
    
    Optional<Student> findByEmail(String email);
    
    List<Student> findBySchoolClass(SchoolClass schoolClass);
    
    List<Student> findByActiveTrue();
    
    List<Student> findBySchoolClassAndActiveTrue(SchoolClass schoolClass);
    
    @Query("SELECT s FROM Student s WHERE s.schoolClass.id = :classId AND s.active = true")
    List<Student> findActiveStudentsByClassId(@Param("classId") Long classId);
    
    @Query("SELECT s FROM Student s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Student> findByNameContaining(@Param("name") String name);
    
    boolean existsByStudentNumber(String studentNumber);
    
    boolean existsByEmail(String email);
    
    long countBySchoolClass(SchoolClass schoolClass);
    
    long countByActiveTrue();
    
    List<Student> findBySchoolClassId(Long classId);
}