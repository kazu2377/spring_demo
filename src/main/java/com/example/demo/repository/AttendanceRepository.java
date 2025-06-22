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
    
    List<Attendance> findByStudentId(Long studentId);
    
    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);
    
    List<Attendance> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.schoolClass.id = :classId AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<Attendance> findBySchoolClassIdAndAttendanceDateBetween(@Param("classId") Long classId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.schoolClass.id = :classId ORDER BY a.attendanceDate DESC")
    List<Attendance> findBySchoolClassIdOrderByAttendanceDateDesc(@Param("classId") Long classId);
    
    List<Attendance> findAllByOrderByAttendanceDateDesc();
    
    // クラスID基準のメソッド
    List<Attendance> findBySchoolClassId(Long classId);
    
    // 日付範囲での検索メソッド
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByStudentIdAndAttendanceDateBetween(@Param("studentId") Long studentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // 統計用メソッド
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.schoolClass.id = :classId")
    long countBySchoolClassId(@Param("classId") Long classId);
    
    // ソート付き検索メソッド
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId ORDER BY a.attendanceDate ASC")
    List<Attendance> findByStudentIdOrderByAttendanceDateAsc(@Param("studentId") Long studentId);
    
    @Query("SELECT a FROM Attendance a WHERE a.schoolClass.id = :classId ORDER BY a.attendanceDate ASC")
    List<Attendance> findBySchoolClassIdOrderByAttendanceDateAsc(@Param("classId") Long classId);
    
    // 最新の出席記録
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId ORDER BY a.attendanceDate DESC LIMIT 1")
    Optional<Attendance> findLatestByStudentId(@Param("studentId") Long studentId);
    
    // 特定期間の全記録
    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate DESC")
    List<Attendance> findAllByAttendanceDateBetweenOrderByAttendanceDateDesc(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}