package models;

import java.io.Serializable;

public class Subject implements Serializable {
    private static final long serialVersionUID = 1L;
    private String subjectId;
    private String subjectName;
    private int maxMarks;
    private int credits;
    private int semester;

    public Subject(String subjectId, String subjectName, int maxMarks) {
        this(subjectId, subjectName, maxMarks, 0, 0);
    }

    public Subject(String subjectId, String subjectName, int maxMarks, int credits, int semester) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.maxMarks = maxMarks;
        this.credits = credits;
        this.semester = semester;
    }
    public String getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public int getMaxMarks() { return maxMarks; }
    public int getCredits() { return credits; }
    public int getSemester() { return semester; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }
    public void setCredits(int credits) { this.credits = credits; }
    public void setSemester(int semester) { this.semester = semester; }
}
