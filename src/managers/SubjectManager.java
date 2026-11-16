package managers;

import java.util.ArrayList;
import java.util.List;
import models.Subject;

public class SubjectManager {
    private final ArrayList<Subject> subjects = new ArrayList<>();
    public void setSubjects(List<Subject> data) { subjects.clear(); if (data != null) subjects.addAll(data); }
    public ArrayList<Subject> getAllSubjects() { return new ArrayList<>(subjects); }
    public Subject searchSubject(String id) {
        for (Subject s : subjects) if (s.getSubjectId().equalsIgnoreCase(id)) return s;
        return null;
    }
    public void addSubject(Subject subject) {
        validate(subject.getSubjectId(), subject.getSubjectName(), subject.getMaxMarks(),
            subject.getCredits(), subject.getSemester());
        if (searchSubject(subject.getSubjectId()) != null) throw new IllegalArgumentException("Subject code already exists.");
        subjects.add(subject);
    }
    public void updateSubject(String id, String name, int maxMarks, int credits, int semester) {
        validate(id, name, maxMarks, credits, semester);
        Subject subject = searchSubject(id);
        if (subject == null) throw new IllegalArgumentException("Subject not found.");
        subject.setSubjectName(name);
        subject.setMaxMarks(maxMarks);
        subject.setCredits(credits);
        subject.setSemester(semester);
    }
    public boolean deleteSubject(String id) { Subject s = searchSubject(id); return s != null && subjects.remove(s); }

    private void validate(String id, String name, int maxMarks, int credits, int semester) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Subject code is required.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Subject name is required.");
        if (maxMarks <= 0) throw new IllegalArgumentException("Maximum marks must be positive.");
        if (credits < 0 || credits > 30) throw new IllegalArgumentException("Credits must be between 0 and 30.");
        if (semester < 0 || semester > 20) throw new IllegalArgumentException("Semester must be between 0 and 20.");
    }
}
