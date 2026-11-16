package models;

public class Student extends Person {
    private static final long serialVersionUID = 1L;
    private int rollNo;
    private String course;
    private int semester;
    private String contactNumber;

    public Student(int rollNo, String name, String email, String course, int semester) {
        this(rollNo, name, email, course, semester, "");
    }

    public Student(int rollNo, String name, String email, String course, int semester, String contactNumber) {
        super(name, email);
        this.rollNo = rollNo;
        this.course = course;
        this.semester = semester;
        this.contactNumber = contactNumber;
    }

    public int getRollNo() { return rollNo; }
    public String getCourse() { return course; }
    public int getSemester() { return semester; }
    public String getContactNumber() { return contactNumber == null ? "" : contactNumber; }
    public void setCourse(String course) { this.course = course; }
    public void setSemester(int semester) { this.semester = semester; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    @Override
    public String getRoleDescription() { return "Student"; }
}
