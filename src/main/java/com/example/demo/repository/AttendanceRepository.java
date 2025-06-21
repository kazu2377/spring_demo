package com.example.demo.repository;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Student;
import com.example.demo.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    Optional<Attendance> findByStudentAndAttendanceDate(Student student, LocalDate attendanceDate);
    
    List<Attendance> findByStudent(Student student);
    
    List<Attendance> findByAttendanceDate(LocalDate attendanceDate);
    
    List<Attendance> findBySchoolClass(SchoolClass schoolClass);
    
    List<Attendance> findBySchoolClassAndAttendanceDate(SchoolClass schoolClass, LocalDate attendanceDate);
    
    List<Attendance> findByStudentAndAttendanceDateBetween(Student student, LocalDate startDate, LocalDate endDate);
    
    List<Attendance> findBySchoolClassAndAttendanceDateBetween(SchoolClass schoolClass, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId AND a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate DESC")
    List<Attendance> findStudentAttendanceInPeriod(@Param("studentId") Long studentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.schoolClass.id = :classId AND a.attendanceDate = :date ORDER BY a.student.lastName, a.student.firstName")
    List<Attendance> findClassAttendanceOnDate(@Param("classId") Long classId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :student AND a.status = 'PRESENT'")
    long countPresentDaysByStudent(@Param("student") Student student);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :student AND a.status = 'ABSENT'")
    long countAbsentDaysByStudent(@Param("student") Student student);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :student AND a.status = 'LATE'")
    long countLateDaysByStudent(@Param("student") Student student);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :student AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.status = 'PRESENT'")
    long countPresentDaysByStudentInPeriod(@Param("student") Student student, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :student AND a.attendanceDate BETWEEN :startDate AND :endDate")
    long countTotalDaysByStudentInPeriod(@Param("student") Student student, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    boolean existsByStudentAndAttendanceDate(Student student, LocalDate attendanceDate);
}