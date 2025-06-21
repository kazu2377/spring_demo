package com.example.demo.service;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Student;
import com.example.demo.entity.SchoolClass;
import com.example.demo.entity.User;
import com.example.demo.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }
    
    public Optional<Attendance> getAttendanceById(Long id) {
        return attendanceRepository.findById(id);
    }
    
    public Optional<Attendance> getAttendanceByStudentAndDate(Student student, LocalDate date) {
        return attendanceRepository.findByStudentAndAttendanceDate(student, date);
    }
    
    public List<Attendance> getAttendanceByStudent(Student student) {
        return attendanceRepository.findByStudent(student);
    }
    
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date);
    }
    
    public List<Attendance> getAttendanceByClass(SchoolClass schoolClass) {
        return attendanceRepository.findBySchoolClass(schoolClass);
    }
    
    public List<Attendance> getClassAttendanceOnDate(Long classId, LocalDate date) {
        return attendanceRepository.findClassAttendanceOnDate(classId, date);
    }
    
    public List<Attendance> getStudentAttendanceInPeriod(Long studentId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findStudentAttendanceInPeriod(studentId, startDate, endDate);
    }
    
    public List<Attendance> getClassAttendanceInPeriod(SchoolClass schoolClass, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findBySchoolClassAndAttendanceDateBetween(schoolClass, startDate, endDate);
    }
    
    public Attendance recordAttendance(Student student, SchoolClass schoolClass, LocalDate date, 
                                     Attendance.AttendanceStatus status, String notes, User recordedBy) {
        
        Optional<Attendance> existingAttendance = attendanceRepository.findByStudentAndAttendanceDate(student, date);
        
        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            attendance.setStatus(status);
            attendance.setNotes(notes);
            attendance.setRecordedBy(recordedBy);
            return attendanceRepository.save(attendance);
        } else {
            Attendance attendance = new Attendance(student, schoolClass, date, status);
            attendance.setNotes(notes);
            attendance.setRecordedBy(recordedBy);
            return attendanceRepository.save(attendance);
        }
    }
    
    public Attendance updateAttendance(Long id, Attendance.AttendanceStatus status, String notes, User recordedBy) {
        return attendanceRepository.findById(id)
            .map(attendance -> {
                attendance.setStatus(status);
                attendance.setNotes(notes);
                attendance.setRecordedBy(recordedBy);
                return attendanceRepository.save(attendance);
            })
            .orElseThrow(() -> new RuntimeException("出席記録が見つかりません: " + id));
    }
    
    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }
    
    public long getPresentDaysByStudent(Student student) {
        return attendanceRepository.countPresentDaysByStudent(student);
    }
    
    public long getAbsentDaysByStudent(Student student) {
        return attendanceRepository.countAbsentDaysByStudent(student);
    }
    
    public long getLateDaysByStudent(Student student) {
        return attendanceRepository.countLateDaysByStudent(student);
    }
    
    public double getAttendanceRate(Student student, LocalDate startDate, LocalDate endDate) {
        long totalDays = attendanceRepository.countTotalDaysByStudentInPeriod(student, startDate, endDate);
        if (totalDays == 0) {
            return 0.0;
        }
        
        long presentDays = attendanceRepository.countPresentDaysByStudentInPeriod(student, startDate, endDate);
        return (double) presentDays / totalDays * 100;
    }
    
    public double getOverallAttendanceRate(Student student) {
        long totalDays = attendanceRepository.findByStudent(student).size();
        if (totalDays == 0) {
            return 0.0;
        }
        
        long presentDays = attendanceRepository.countPresentDaysByStudent(student);
        return (double) presentDays / totalDays * 100;
    }
    
    public boolean hasAttendanceRecord(Student student, LocalDate date) {
        return attendanceRepository.existsByStudentAndAttendanceDate(student, date);
    }
    
    public void createBulkAttendance(List<Student> students, SchoolClass schoolClass, LocalDate date, 
                                   Attendance.AttendanceStatus defaultStatus, User recordedBy) {
        for (Student student : students) {
            if (!hasAttendanceRecord(student, date)) {
                recordAttendance(student, schoolClass, date, defaultStatus, "", recordedBy);
            }
        }
    }
    
    public AttendanceSummary getAttendanceSummary(Student student) {
        long presentDays = getPresentDaysByStudent(student);
        long absentDays = getAbsentDaysByStudent(student);
        long lateDays = getLateDaysByStudent(student);
        long totalDays = presentDays + absentDays + lateDays;
        
        double attendanceRate = totalDays > 0 ? (double) presentDays / totalDays * 100 : 0.0;
        
        return new AttendanceSummary(presentDays, absentDays, lateDays, totalDays, attendanceRate);
    }
    
    public static class AttendanceSummary {
        private final long presentDays;
        private final long absentDays;
        private final long lateDays;
        private final long totalDays;
        private final double attendanceRate;
        
        public AttendanceSummary(long presentDays, long absentDays, long lateDays, long totalDays, double attendanceRate) {
            this.presentDays = presentDays;
            this.absentDays = absentDays;
            this.lateDays = lateDays;
            this.totalDays = totalDays;
            this.attendanceRate = attendanceRate;
        }
        
        public long getPresentDays() { return presentDays; }
        public long getAbsentDays() { return absentDays; }
        public long getLateDays() { return lateDays; }
        public long getTotalDays() { return totalDays; }
        public double getAttendanceRate() { return attendanceRate; }
    }
}