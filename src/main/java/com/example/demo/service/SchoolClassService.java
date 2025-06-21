package com.example.demo.service;

import com.example.demo.entity.SchoolClass;
import com.example.demo.entity.User;
import com.example.demo.repository.SchoolClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SchoolClassService {
    
    @Autowired
    private SchoolClassRepository schoolClassRepository;
    
    public List<SchoolClass> getAllClasses() {
        return schoolClassRepository.findAll();
    }
    
    public List<SchoolClass> getActiveClasses() {
        return schoolClassRepository.findByActiveTrue();
    }
    
    public List<SchoolClass> getCurrentActiveClasses() {
        return schoolClassRepository.findCurrentActiveClasses();
    }
    
    public Optional<SchoolClass> getClassById(Long id) {
        return schoolClassRepository.findById(id);
    }
    
    public Optional<SchoolClass> getClassByName(String className) {
        return schoolClassRepository.findByClassName(className);
    }
    
    public List<SchoolClass> getClassesByTeacher(User teacher) {
        return schoolClassRepository.findByTeacherAndActiveTrue(teacher);
    }
    
    public List<SchoolClass> getClassesBySubject(String subject) {
        return schoolClassRepository.findBySubject(subject);
    }
    
    public List<SchoolClass> getActiveClassesOnDate(LocalDate date) {
        return schoolClassRepository.findActiveClassesOnDate(date);
    }
    
    public List<SchoolClass> searchClasses(String searchTerm) {
        return schoolClassRepository.findByClassNameOrSubjectContaining(searchTerm);
    }
    
    public SchoolClass createClass(SchoolClass schoolClass) {
        if (schoolClassRepository.existsByClassName(schoolClass.getClassName())) {
            throw new RuntimeException("クラス名が既に存在します: " + schoolClass.getClassName());
        }
        
        if (schoolClass.getStartDate().isAfter(schoolClass.getEndDate())) {
            throw new RuntimeException("開始日は終了日より前である必要があります");
        }
        
        return schoolClassRepository.save(schoolClass);
    }
    
    public SchoolClass updateClass(Long id, SchoolClass updatedClass) {
        return schoolClassRepository.findById(id)
            .map(schoolClass -> {
                schoolClass.setClassName(updatedClass.getClassName());
                schoolClass.setSubject(updatedClass.getSubject());
                schoolClass.setDescription(updatedClass.getDescription());
                schoolClass.setStartDate(updatedClass.getStartDate());
                schoolClass.setEndDate(updatedClass.getEndDate());
                schoolClass.setTeacher(updatedClass.getTeacher());
                schoolClass.setMaxStudents(updatedClass.getMaxStudents());
                
                if (schoolClass.getStartDate().isAfter(schoolClass.getEndDate())) {
                    throw new RuntimeException("開始日は終了日より前である必要があります");
                }
                
                return schoolClassRepository.save(schoolClass);
            })
            .orElseThrow(() -> new RuntimeException("クラスが見つかりません: " + id));
    }
    
    public void deactivateClass(Long id) {
        schoolClassRepository.findById(id)
            .ifPresentOrElse(
                schoolClass -> {
                    schoolClass.setActive(false);
                    schoolClassRepository.save(schoolClass);
                },
                () -> {
                    throw new RuntimeException("クラスが見つかりません: " + id);
                }
            );
    }
    
    public void reactivateClass(Long id) {
        schoolClassRepository.findById(id)
            .ifPresentOrElse(
                schoolClass -> {
                    schoolClass.setActive(true);
                    schoolClassRepository.save(schoolClass);
                },
                () -> {
                    throw new RuntimeException("クラスが見つかりません: " + id);
                }
            );
    }
    
    public void assignTeacher(Long classId, User teacher) {
        schoolClassRepository.findById(classId)
            .ifPresentOrElse(
                schoolClass -> {
                    schoolClass.setTeacher(teacher);
                    schoolClassRepository.save(schoolClass);
                },
                () -> {
                    throw new RuntimeException("クラスが見つかりません: " + classId);
                }
            );
    }
    
    public long getTotalActiveClassCount() {
        return schoolClassRepository.countByActiveTrue();
    }
    
    public long getClassCountByTeacher(User teacher) {
        return schoolClassRepository.countByTeacherAndActiveTrue(teacher);
    }
    
    public boolean isClassNameAvailable(String className) {
        return !schoolClassRepository.existsByClassName(className);
    }
    
    public boolean isClassFull(Long classId) {
        return schoolClassRepository.findById(classId)
            .map(schoolClass -> {
                if (schoolClass.getMaxStudents() == null) {
                    return false;
                }
                return schoolClass.getCurrentStudentCount() >= schoolClass.getMaxStudents();
            })
            .orElse(false);
    }
    
    public boolean canAddStudent(Long classId) {
        return !isClassFull(classId);
    }
}