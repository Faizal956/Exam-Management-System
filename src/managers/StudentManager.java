package managers;

import java.util.ArrayList;
import java.util.List;
import models.Student;

public class StudentManager {
    private final ArrayList<Student> students = new ArrayList<>();

    public void setStudents(List<Student> data) {
        students.clear();
        if (data != null) students.addAll(data);
    }
    public ArrayList<Student> getAllStudents() { return new ArrayList<>(students); }
    public Student searchStudent(int rollNo) {
        for (Student s : students) if (s.getRollNo() == rollNo) return s;
        return null;
    }
    public void addStudent(Student student) {
        validateStudent(student.getRollNo(), student.getName(), student.getEmail(),
            student.getCourse(), student.getSemester(), student.getContactNumber());
        if (searchStudent(student.getRollNo()) != null)
            throw new IllegalArgumentException("Roll number already exists.");
        students.add(student);
    }
    public void updateStudent(int rollNo, String name, String email, String course, int semester) {
        Student existing = searchStudent(rollNo);
        updateStudent(rollNo, name, email, course, semester,
            existing == null ? "" : existing.getContactNumber());
    }
    public void updateStudent(int rollNo, String name, String email, String course,
            int semester, String contactNumber) {
        validateStudent(rollNo, name, email, course, semester, contactNumber);
        Student s = searchStudent(rollNo);
        if (s == null) throw new IllegalArgumentException("Student not found.");
        s.setName(name); s.setEmail(email); s.setCourse(course); s.setSemester(semester);
        s.setContactNumber(contactNumber);
    }
    public boolean deleteStudent(int rollNo) {
        Student s = searchStudent(rollNo);
        return s != null && students.remove(s);
    }

    private void validateStudent(int rollNo, String name, String email, String course,
            int semester, String contactNumber) {
        if (rollNo <= 0) throw new IllegalArgumentException("Roll number must be positive.");
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Student name is required.");
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Enter a valid email address.");
        if (course == null || course.trim().isEmpty())
            throw new IllegalArgumentException("Course is required.");
        if (semester <= 0) throw new IllegalArgumentException("Semester must be positive.");
        if (contactNumber != null && !contactNumber.isBlank()
                && !contactNumber.matches("[+0-9() .-]{7,20}"))
            throw new IllegalArgumentException("Enter a valid contact number.");
    }
}
