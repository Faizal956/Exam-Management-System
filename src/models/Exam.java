package models;

import java.io.Serializable;

public class Exam implements Serializable {
    private static final long serialVersionUID = 1L;
    private int examId;
    private String examName;
    private String examDate;
    private String examTime;
    private String examType;
    private String examStatus;
    private String subjectId;
    private int maxMarks;

    public Exam(int examId, String examName, String examDate) {
        this(examId, examName, examDate, "09:00", "Final", "Scheduled", "", 0);
    }

    public Exam(int examId, String examName, String examDate, String examTime,
            String examType, String examStatus) {
        this(examId, examName, examDate, examTime, examType, examStatus, "", 0);
    }

    public Exam(int examId, String examName, String examDate, String examTime,
            String examType, String examStatus, String subjectId, int maxMarks) {
        this.examId = examId;
        this.examName = examName;
        this.examDate = examDate;
        this.examTime = examTime;
        this.examType = examType;
        this.examStatus = examStatus;
        this.subjectId = subjectId;
        this.maxMarks = maxMarks;
    }
    public int getExamId() { return examId; }
    public String getExamName() { return examName; }
    public String getExamDate() { return examDate; }
    public String getExamTime() { return examTime == null || examTime.isBlank() ? "09:00" : examTime; }
    public String getExamType() { return examType == null || examType.isBlank() ? "Final" : examType; }
    public String getExamStatus() { return examStatus == null || examStatus.isBlank() ? "Scheduled" : examStatus; }
    public String getSubjectId() { return subjectId == null ? "" : subjectId; }
    public int getMaxMarks() { return maxMarks; }
    public void setExamName(String examName) { this.examName = examName; }
    public void setExamDate(String examDate) { this.examDate = examDate; }
    public void setExamTime(String examTime) { this.examTime = examTime; }
    public void setExamType(String examType) { this.examType = examType; }
    public void setExamStatus(String examStatus) { this.examStatus = examStatus; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }
}
