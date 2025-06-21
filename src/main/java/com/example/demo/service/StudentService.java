package com.example.demo.service;

import com.example.demo.entity.Student;
import com.example.demo.entity.SchoolClass;
import com.example.demo.entity.User;
import com.example.demo.entity.Role;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
    
    public List<Student> getActiveStudents() {
        return studentRepository.findByActiveTrue();
    }
    
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }
    
    public Optional<Student> getStudentByStudentNumber(String studentNumber) {
        return studentRepository.findByStudentNumber(studentNumber);
    }
    
    public List<Student> getStudentsByClass(SchoolClass schoolClass) {
        return studentRepository.findBySchoolClassAndActiveTrue(schoolClass);
    }
    
    public List<Student> searchStudentsByName(String name) {
        return studentRepository.findByNameContaining(name);
    }
    
    public Student createStudent(Student student, String username, String password) {
        if (studentRepository.existsByStudentNumber(student.getStudentNumber())) {
            throw new RuntimeException("学籍番号が既に存在します: " + student.getStudentNumber());
        }
        
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new RuntimeException("メールアドレスが既に存在します: " + student.getEmail());
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        
        User savedUser = userRepository.save(user);
        student.setUser(savedUser);
        
        return studentRepository.save(student);
    }
    
    public Student updateStudent(Long id, Student updatedStudent) {
        return studentRepository.findById(id)
            .map(student -> {
                student.setFirstName(updatedStudent.getFirstName());
                student.setLastName(updatedStudent.getLastName());
                student.setEmail(updatedStudent.getEmail());
                student.setPhoneNumber(updatedStudent.getPhoneNumber());
                student.setSchoolClass(updatedStudent.getSchoolClass());
                return studentRepository.save(student);
            })
            .orElseThrow(() -> new RuntimeException("受講生が見つかりません: " + id));
    }
    
    public void deactivateStudent(Long id) {
        studentRepository.findById(id)
            .ifPresentOrElse(
                student -> {
                    student.setActive(false);
                    if (student.getUser() != null) {
                        student.getUser().setEnabled(false);
                        userRepository.save(student.getUser());
                    }
                    studentRepository.save(student);
                },
                () -> {
                    throw new RuntimeException("受講生が見つかりません: " + id);
                }
            );
    }
    
    public void reactivateStudent(Long id) {
        studentRepository.findById(id)
            .ifPresentOrElse(
                student -> {
                    student.setActive(true);
                    if (student.getUser() != null) {
                        student.getUser().setEnabled(true);
                        userRepository.save(student.getUser());
                    }
                    studentRepository.save(student);
                },
                () -> {
                    throw new RuntimeException("受講生が見つかりません: " + id);
                }
            );
    }
    
    public void assignToClass(Long studentId, SchoolClass schoolClass) {
        studentRepository.findById(studentId)
            .ifPresentOrElse(
                student -> {
                    student.setSchoolClass(schoolClass);
                    studentRepository.save(student);
                },
                () -> {
                    throw new RuntimeException("受講生が見つかりません: " + studentId);
                }
            );
    }
    
    public long getStudentCountByClass(SchoolClass schoolClass) {
        return studentRepository.countBySchoolClass(schoolClass);
    }
    
    public long getTotalActiveStudentCount() {
        return studentRepository.countByActiveTrue();
    }
    
    public String generateStudentNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count = studentRepository.count() + 1;
        return year + String.format("%04d", count);
    }
    
    public boolean isStudentNumberAvailable(String studentNumber) {
        return !studentRepository.existsByStudentNumber(studentNumber);
    }
    
    public boolean isEmailAvailable(String email) {
        return !studentRepository.existsByEmail(email);
    }
}