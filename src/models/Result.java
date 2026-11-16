package models;

import java.io.Serializable;

public class Result implements Serializable {
    private static final long serialVersionUID = 1L;
    private Student student;
    private Subject subject;
    private Exam exam;
    private double marksObtained;
    private boolean published;

    public Result(Student student, Subject subject, Exam exam, double marksObtained) {
        if (student == null || subject == null || exam == null)
            throw new IllegalArgumentException("Select a valid student, subject and exam.");
        if (!Double.isFinite(marksObtained))
            throw new IllegalArgumentException("Marks must be a finite number.");
        int maxMarks = exam.getMaxMarks() > 0 ? exam.getMaxMarks() : subject.getMaxMarks();
        if (marksObtained < 0 || marksObtained > maxMarks)
            throw new IllegalArgumentException("Marks must be between 0 and " + maxMarks + ".");
        this.student = student;
        this.subject = subject;
        this.exam = exam;
        this.marksObtained = marksObtained;
    }
    public Student getStudent() { return student; }
    public Subject getSubject() { return subject; }
    public Exam getExam() { return exam; }
    public double getMarksObtained() { return marksObtained; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public double calculatePercentage() {
        int maxMarks = exam.getMaxMarks() > 0 ? exam.getMaxMarks() : subject.getMaxMarks();
        return marksObtained * 100.0 / maxMarks;
    }
    public boolean isPassing() { return calculatePercentage() >= 40; }
    public String calculateGrade() {
        double p = calculatePercentage();
        if (p >= 90) return "A+";
        if (p >= 80) return "A";
        if (p >= 70) return "B";
        if (p >= 60) return "C";
        if (p >= 40) return "D";
        return "F";
    }
}
