package managers;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import models.Exam;

public class ExamManager {
    private final ArrayList<Exam> exams = new ArrayList<>();
    public void setExams(List<Exam> data) { exams.clear(); if (data != null) exams.addAll(data); }
    public ArrayList<Exam> getAllExams() { return new ArrayList<>(exams); }
    public Exam searchExam(int id) { for (Exam e : exams) if (e.getExamId() == id) return e; return null; }
    public void addExam(Exam exam) {
        validate(exam.getExamId(), exam.getExamName(), exam.getExamDate(),
            exam.getExamTime(), exam.getExamType(), exam.getExamStatus(),
            exam.getSubjectId(), exam.getMaxMarks());
        if (searchExam(exam.getExamId()) != null) throw new IllegalArgumentException("Exam ID already exists.");
        exams.add(exam);
    }
    public void updateExam(int id, String name, String date, String time, String type, String status) {
        updateExam(id, name, date, time, type, status, "", 0);
    }
    public void updateExam(int id, String name, String date, String time, String type,
            String status, String subjectId, int maxMarks) {
        validate(id, name, date, time, type, status, subjectId, maxMarks);
        Exam exam = searchExam(id);
        if (exam == null) throw new IllegalArgumentException("Examination not found.");
        exam.setExamName(name);
        exam.setExamDate(date);
        exam.setExamTime(time);
        exam.setExamType(type);
        exam.setExamStatus(status);
        exam.setSubjectId(subjectId);
        exam.setMaxMarks(maxMarks);
    }
    public boolean deleteExam(int id) { Exam e = searchExam(id); return e != null && exams.remove(e); }

    private void validate(int id, String name, String date, String time, String type,
            String status, String subjectId, int maxMarks) {
        if (id <= 0) throw new IllegalArgumentException("Exam ID must be positive.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Examination name is required.");
        if (type == null || type.trim().isEmpty()) throw new IllegalArgumentException("Examination type is required.");
        if (status == null || status.trim().isEmpty()) throw new IllegalArgumentException("Examination status is required.");
        if (maxMarks < 0) throw new IllegalArgumentException("Maximum marks cannot be negative.");
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("Enter the examination date as YYYY-MM-DD.");
        }
        try {
            LocalTime.parse(time);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("Enter the examination time as HH:MM (24-hour time).");
        }
    }
}
