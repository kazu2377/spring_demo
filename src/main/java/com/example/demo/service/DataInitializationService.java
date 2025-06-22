package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class DataInitializationService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private SchoolClassRepository schoolClassRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private TestRepository testRepository;
    
    @Autowired
    private TestScoreRepository testScoreRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final Random random = new Random();

    public void initializeSampleData() {
        if (userRepository.count() > 4) {
            // サンプルデータが既に存在する場合はスキップ
            return;
        }
        
        // 1. ユーザー（講師・管理者）を作成
        createUsers();
        
        // 2. クラスを作成
        List<SchoolClass> classes = createClasses();
        
        // 3. 受講生を作成
        List<Student> students = createStudents(classes);
        
        // 4. テストを作成
        List<Test> tests = createTests(classes);
        
        // 5. 出欠席記録を作成
        createAttendanceRecords(students, classes);
        
        // 6. テスト成績を作成
        createTestScores(students, tests);
    }

    private void createUsers() {
        // 管理者
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);

        // 講師
        String[] teacherNames = {"田中先生", "佐藤先生", "鈴木先生", "高橋先生"};
        for (int i = 0; i < teacherNames.length; i++) {
            User teacher = new User();
            teacher.setUsername("teacher" + (i + 1));
            teacher.setPassword(passwordEncoder.encode("teacher123"));
            teacher.setRole(Role.TEACHER);
            teacher.setActive(true);
            userRepository.save(teacher);
        }

        // 受講生用ユーザー（後で受講生エンティティと関連付け）
        String[] studentUsernames = {
            "yamada", "tanaka", "suzuki", "takahashi", "watanabe", 
            "ito", "nakamura", "kobayashi", "kato", "yoshida",
            "yamamoto", "sasaki", "matsumoto", "inoue", "kimura",
            "hayashi", "shimizu", "yamazaki", "mori", "abe"
        };
        
        for (String username : studentUsernames) {
            User studentUser = new User();
            studentUser.setUsername(username);
            studentUser.setPassword(passwordEncoder.encode("student123"));
            studentUser.setRole(Role.STUDENT);
            studentUser.setActive(true);
            userRepository.save(studentUser);
        }
    }

    private List<SchoolClass> createClasses() {
        List<User> teachers = userRepository.findByRole(Role.TEACHER);
        
        String[][] classData = {
            {"Java基礎コース2024春", "プログラミング基礎", "Javaの基本文法からオブジェクト指向まで"},
            {"Web開発実践コース", "Web開発", "HTML/CSS/JavaScriptを使った実践的なWeb開発"},
            {"データベース設計コース", "データベース", "SQL基礎からデータベース設計まで"},
            {"システム開発プロジェクト", "総合実習", "実際のシステム開発を通じた総合学習"}
        };

        List<SchoolClass> classes = Arrays.asList(
            createSchoolClass(classData[0][0], classData[0][1], classData[0][2], 
                            LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(4), 
                            teachers.get(0), 25),
            createSchoolClass(classData[1][0], classData[1][1], classData[1][2], 
                            LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(5), 
                            teachers.get(1), 20),
            createSchoolClass(classData[2][0], classData[2][1], classData[2][2], 
                            LocalDate.now().minusWeeks(3), LocalDate.now().plusMonths(3), 
                            teachers.get(2), 15),
            createSchoolClass(classData[3][0], classData[3][1], classData[3][2], 
                            LocalDate.now().plusWeeks(1), LocalDate.now().plusMonths(6), 
                            teachers.get(3), 12)
        );

        return schoolClassRepository.saveAll(classes);
    }

    private SchoolClass createSchoolClass(String className, String subject, String description,
                                        LocalDate startDate, LocalDate endDate, User teacher, int maxStudents) {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setClassName(className);
        schoolClass.setSubject(subject);
        schoolClass.setDescription(description);
        schoolClass.setStartDate(startDate);
        schoolClass.setEndDate(endDate);
        schoolClass.setTeacher(teacher);
        schoolClass.setMaxStudents(maxStudents);
        schoolClass.setActive(true);
        return schoolClass;
    }

    private List<Student> createStudents(List<SchoolClass> classes) {
        List<User> studentUsers = userRepository.findByRole(Role.STUDENT);
        String[] firstNames = {
            "太郎", "花子", "次郎", "美咲", "三郎", "由美", "四郎", "香織", "五郎", "真理",
            "六郎", "恵子", "七郎", "裕子", "八郎", "智子", "九郎", "康子", "十郎", "明美"
        };
        String[] lastNames = {
            "山田", "田中", "鈴木", "高橋", "渡辺", "伊藤", "中村", "小林", "加藤", "吉田",
            "山本", "佐々木", "松本", "井上", "木村", "林", "清水", "山崎", "森", "阿部"
        };

        List<Student> students = new java.util.ArrayList<>();
        
        for (int i = 0; i < Math.min(studentUsers.size(), 20); i++) {
            Student student = new Student();
            student.setStudentNumber("S" + String.format("%04d", 2024001 + i));
            student.setLastName(lastNames[i]);
            student.setFirstName(firstNames[i]);
            student.setEmail(studentUsers.get(i).getUsername() + "@example.com");
            student.setEnrollmentDate(LocalDate.now().minusMonths(random.nextInt(6) + 1));
            student.setActive(true);
            student.setUser(studentUsers.get(i));
            
            // クラスに分散して配置
            if (i < 8) {
                student.setSchoolClass(classes.get(0)); // Java基礎コース
            } else if (i < 14) {
                student.setSchoolClass(classes.get(1)); // Web開発実践
            } else if (i < 18) {
                student.setSchoolClass(classes.get(2)); // データベース設計
            } else {
                student.setSchoolClass(classes.get(3)); // システム開発プロジェクト
            }
            
            students.add(student);
        }

        return studentRepository.saveAll(students);
    }

    private List<Test> createTests(List<SchoolClass> classes) {
        List<Test> tests = new java.util.ArrayList<>();
        
        for (SchoolClass schoolClass : classes) {
            // 各クラスに3〜5個のテストを作成
            int testCount = random.nextInt(3) + 3;
            
            for (int i = 0; i < testCount; i++) {
                Test test = new Test();
                test.setTestName(getTestName(schoolClass.getSubject(), i + 1));
                test.setDescription(getTestDescription(schoolClass.getSubject(), i + 1));
                test.setSchoolClass(schoolClass);
                test.setTestDate(getRandomTestDate(schoolClass));
                test.setTestTime(LocalTime.of(10, 0)); // 10:00開始
                test.setDuration(90); // 90分
                test.setMaxScore(100);
                test.setPassingScore(60);
                test.setTestType(getRandomTestType(i));
                test.setActive(true);
                
                tests.add(test);
            }
        }

        return testRepository.saveAll(tests);
    }

    private String getTestName(String subject, int number) {
        switch (subject) {
            case "プログラミング基礎":
                String[] javaTests = {"Java基本文法テスト", "オブジェクト指向理解度テスト", "例外処理・IO操作テスト", "コレクション・ジェネリクステスト", "総合演習テスト"};
                return javaTests[Math.min(number - 1, javaTests.length - 1)];
            case "Web開発":
                String[] webTests = {"HTML/CSS基礎テスト", "JavaScript基礎テスト", "DOM操作・イベント処理テスト", "Ajax・API連携テスト", "Web開発総合テスト"};
                return webTests[Math.min(number - 1, webTests.length - 1)];
            case "データベース":
                String[] dbTests = {"SQL基礎テスト", "テーブル設計テスト", "結合・副問合せテスト", "正規化・パフォーマンステスト"};
                return dbTests[Math.min(number - 1, dbTests.length - 1)];
            default:
                return subject + " 第" + number + "回テスト";
        }
    }

    private String getTestDescription(String subject, int number) {
        switch (subject) {
            case "プログラミング基礎":
                return "Java プログラミングの理解度を確認するテストです。";
            case "Web開発":
                return "Web 開発技術の習得状況を評価します。";
            case "データベース":
                return "データベース設計・操作の理解度をチェックします。";
            default:
                return subject + "の学習内容に関するテストです。";
        }
    }

    private LocalDate getRandomTestDate(SchoolClass schoolClass) {
        LocalDate start = schoolClass.getStartDate();
        LocalDate end = schoolClass.getEndDate();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        long randomDays = random.nextLong(daysBetween + 1);
        return start.plusDays(randomDays);
    }

    private TestType getRandomTestType(int index) {
        TestType[] types = {TestType.QUIZ, TestType.MIDTERM, TestType.FINAL, TestType.ASSIGNMENT, TestType.PRACTICAL, TestType.OTHER};
        if (index == 0) return TestType.QUIZ;
        if (index == 1) return TestType.MIDTERM;
        if (index >= 4) return TestType.FINAL;
        return types[random.nextInt(types.length)];
    }

    private void createAttendanceRecords(List<Student> students, List<SchoolClass> classes) {
        LocalDate today = LocalDate.now();
        
        for (Student student : students) {
            SchoolClass studentClass = student.getSchoolClass();
            if (studentClass == null) continue;
            
            // 過去2ヶ月分の出席記録を作成
            LocalDate startDate = today.minusMonths(2);
            LocalDate currentDate = startDate;
            
            while (currentDate.isBefore(today)) {
                // 平日のみ授業があると仮定
                if (currentDate.getDayOfWeek().getValue() <= 5) {
                    Attendance attendance = new Attendance();
                    attendance.setStudent(student);
                    attendance.setSchoolClass(studentClass);
                    attendance.setAttendanceDate(currentDate);
                    attendance.setStatus(getRandomAttendanceStatus(student));
                    
                    if (attendance.getStatus() == AttendanceStatus.LATE) {
                        attendance.setNotes("遅刻理由: " + getRandomLateReason());
                    } else if (attendance.getStatus() == AttendanceStatus.ABSENT) {
                        attendance.setNotes("欠席理由: " + getRandomAbsentReason());
                    } else if (attendance.getStatus() == AttendanceStatus.EARLY_LEAVE) {
                        attendance.setNotes("早退理由: " + getRandomEarlyLeaveReason());
                    } else if (attendance.getStatus() == AttendanceStatus.EXCUSED) {
                        attendance.setNotes("公欠理由: " + getRandomExcusedReason());
                    }
                    
                    attendance.setRecordedBy(studentClass.getTeacher());
                    attendance.setRecordedAt(LocalDateTime.of(currentDate, LocalTime.of(9, 0)));
                    
                    attendanceRepository.save(attendance);
                }
                currentDate = currentDate.plusDays(1);
            }
        }
    }

    private AttendanceStatus getRandomAttendanceStatus(Student student) {
        // 学生によって出席傾向を変える
        int studentId = student.getId().intValue();
        int baseAttendanceRate = 70 + (studentId % 25); // 70-95%の出席率
        
        int rand = random.nextInt(100);
        if (rand < baseAttendanceRate) {
            return AttendanceStatus.PRESENT;
        } else if (rand < baseAttendanceRate + 8) {
            return AttendanceStatus.LATE;
        } else if (rand < baseAttendanceRate + 10) {
            return AttendanceStatus.EARLY_LEAVE;
        } else if (rand < baseAttendanceRate + 12) {
            return AttendanceStatus.EXCUSED;
        } else {
            return AttendanceStatus.ABSENT;
        }
    }

    private String getRandomLateReason() {
        String[] reasons = {"交通機関の遅延", "寝坊", "体調不良", "急用", "家庭の事情"};
        return reasons[random.nextInt(reasons.length)];
    }

    private String getRandomAbsentReason() {
        String[] reasons = {"体調不良", "家庭の事情", "冠婚葬祭", "就職活動", "通院"};
        return reasons[random.nextInt(reasons.length)];
    }
    
    private String getRandomEarlyLeaveReason() {
        String[] reasons = {"体調不良", "通院", "急用", "家庭の事情", "就職面接"};
        return reasons[random.nextInt(reasons.length)];
    }
    
    private String getRandomExcusedReason() {
        String[] reasons = {"忌引き", "公式行事", "就職活動", "インターンシップ", "学校行事"};
        return reasons[random.nextInt(reasons.length)];
    }

    private void createTestScores(List<Student> students, List<Test> tests) {
        for (Test test : tests) {
            SchoolClass testClass = test.getSchoolClass();
            
            // そのクラスの受講生のみ対象
            for (Student student : students) {
                if (student.getSchoolClass() != null && 
                    student.getSchoolClass().getId().equals(testClass.getId())) {
                    
                    TestScore testScore = new TestScore();
                    testScore.setStudent(student);
                    testScore.setTest(test);
                    testScore.setScore(generateRealisticScore(student, test));
                    testScore.setComment(generateComment(testScore.getScore(), test.getPassingScore()));
                    testScore.setRecordedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
                    testScore.setRecordedBy(testClass.getTeacher());
                    
                    testScoreRepository.save(testScore);
                }
            }
        }
    }

    private Integer generateRealisticScore(Student student, Test test) {
        // 学生の基本的な学力を学籍番号から推定
        int studentId = student.getId().intValue();
        int baseAbility = 50 + (studentId % 40); // 50-90の基本能力
        
        // テストタイプによる難易度調整
        int difficulty = switch (test.getTestType()) {
            case QUIZ -> 10; // 小テストは易しい
            case MIDTERM -> 0; // 中間テストは標準
            case FINAL -> -15; // 期末テストは難しい
            case ASSIGNMENT -> 15; // 課題は高得点
            case PRACTICAL -> -5; // 実技テストは少し難しい
            case OTHER -> 5;
        };
        
        // ランダム要素を追加
        int randomFactor = random.nextInt(21) - 10; // -10から+10
        
        int score = baseAbility + difficulty + randomFactor;
        
        // 0-100の範囲に収める
        return Math.max(0, Math.min(test.getMaxScore(), score));
    }

    private String generateComment(Integer score, Integer passingScore) {
        if (passingScore == null) {
            return "頑張りました。";
        }
        
        if (score >= passingScore + 20) {
            return "優秀な成績です。この調子で頑張ってください。";
        } else if (score >= passingScore + 10) {
            return "良い成績です。継続して学習を続けてください。";
        } else if (score >= passingScore) {
            return "合格です。さらなる向上を目指しましょう。";
        } else if (score >= passingScore - 10) {
            return "惜しい結果でした。復習して再挑戦してください。";
        } else {
            return "基礎からしっかり復習することをお勧めします。";
        }
    }

    @Transactional
    public void clearAllData() {
        testScoreRepository.deleteAll();
        attendanceRepository.deleteAll();
        testRepository.deleteAll();
        studentRepository.deleteAll();
        schoolClassRepository.deleteAll();
        
        // 管理者以外のユーザーを削除
        userRepository.deleteByUsernameNot("admin");
    }
}