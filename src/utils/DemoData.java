package utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import managers.ExamManager;
import managers.ResultManager;
import managers.StudentManager;
import managers.SubjectManager;
import models.Exam;
import models.Result;
import models.Student;
import models.Subject;

public final class DemoData {
    private DemoData() {}

    public static void addTo(StudentManager students, SubjectManager subjects,
            ExamManager exams, ResultManager results) {
        if (isPresent(students, subjects, exams)) {
            throw new IllegalStateException("Demo records are already present. They were not added again.");
        }
        ensureDemoSubjectCodesAvailable(subjects.getAllSubjects());

        List<Student> demoStudents = createStudents(nextStudentId(students.getAllStudents()));
        List<Subject> demoSubjects = createSubjects();
        List<Exam> demoExams = createExams(nextExamId(exams.getAllExams()), demoSubjects);
        List<Result> demoResults = createResults(demoStudents, demoSubjects, demoExams);

        for (Student student : demoStudents) students.addStudent(student);
        for (Subject subject : demoSubjects) subjects.addSubject(subject);
        for (Exam exam : demoExams) exams.addExam(exam);
        for (Result result : demoResults) results.addResult(result);
        results.publishResult(demoResults.get(0));
        results.publishResult(demoResults.get(1));

        FileManager.save("students.dat", students.getAllStudents());
        FileManager.save("subjects.dat", subjects.getAllSubjects());
        FileManager.save("exams.dat", exams.getAllExams());
        FileManager.save("results.dat", results.getAllResults());
    }

    public static boolean isPresent(StudentManager students, SubjectManager subjects, ExamManager exams) {
        return hasDemoData(students.getAllStudents(), subjects.getAllSubjects(), exams.getAllExams());
    }

    private static boolean hasDemoData(List<Student> students, List<Subject> subjects, List<Exam> exams) {
        for (Student student : students) {
            if (student.getEmail().startsWith("demo.student.")) return true;
        }
        for (Subject subject : subjects) {
            if (subject.getSubjectId().startsWith("DEMO-")) return true;
        }
        for (Exam exam : exams) {
            if (exam.getExamName().startsWith("Demo ")) return true;
        }
        return false;
    }

    private static void ensureDemoSubjectCodesAvailable(List<Subject> subjects) {
        String[] codes = {"DEMO-CS101", "DEMO-MA201", "DEMO-PH101"};
        for (String code : codes) {
            for (Subject subject : subjects) {
                if (subject.getSubjectId().equalsIgnoreCase(code)) {
                    throw new IllegalStateException("Subject code " + code
                        + " is already in use; demo records were not added.");
                }
            }
        }
    }

    private static List<Student> createStudents(int firstId) {
        ArrayList<Student> students = new ArrayList<>();
        students.add(new Student(firstId, "Ava Martinez",
            "demo.student." + firstId + "@example.edu", "Computer Science", 2, "+1 555 0101"));
        students.add(new Student(firstId + 1, "Noah Williams",
            "demo.student." + (firstId + 1) + "@example.edu", "Applied Mathematics", 3, "+1 555 0102"));
        students.add(new Student(firstId + 2, "Mia Chen",
            "demo.student." + (firstId + 2) + "@example.edu", "Physics", 2, "+1 555 0103"));
        return students;
    }

    private static List<Subject> createSubjects() {
        ArrayList<Subject> subjects = new ArrayList<>();
        subjects.add(new Subject("DEMO-CS101", "Introduction to Computer Science", 100, 4, 2));
        subjects.add(new Subject("DEMO-MA201", "Applied Mathematics", 100, 3, 3));
        subjects.add(new Subject("DEMO-PH101", "Foundations of Physics", 100, 4, 2));
        return subjects;
    }

    private static List<Exam> createExams(int firstId, List<Subject> subjects) {
        LocalDate today = LocalDate.now();
        ArrayList<Exam> exams = new ArrayList<>();
        exams.add(new Exam(firstId, "Demo Computer Science Midterm", today.plusDays(7).toString(),
            "09:00", "Midterm", "Scheduled", subjects.get(0).getSubjectId(), 100));
        exams.add(new Exam(firstId + 1, "Demo Mathematics Final", today.plusDays(14).toString(),
            "13:30", "Final", "Scheduled", subjects.get(1).getSubjectId(), 100));
        exams.add(new Exam(firstId + 2, "Demo Physics Practical", today.plusDays(21).toString(),
            "10:00", "Practical", "Scheduled", subjects.get(2).getSubjectId(), 100));
        return exams;
    }

    private static List<Result> createResults(List<Student> students,
            List<Subject> subjects, List<Exam> exams) {
        ArrayList<Result> results = new ArrayList<>();
        results.add(new Result(students.get(0), subjects.get(0), exams.get(0), 86));
        results.add(new Result(students.get(1), subjects.get(1), exams.get(1), 74));
        results.add(new Result(students.get(2), subjects.get(2), exams.get(2), 91));
        return results;
    }

    private static int nextStudentId(List<Student> students) {
        int nextId = 9001;
        for (Student student : students) nextId = Math.max(nextId, student.getRollNo() + 1);
        return nextId;
    }

    private static int nextExamId(List<Exam> exams) {
        int nextId = 9001;
        for (Exam exam : exams) nextId = Math.max(nextId, exam.getExamId() + 1);
        return nextId;
    }
}
