package managers;

import java.util.ArrayList;
import java.util.List;
import models.Result;

public class ResultManager {
    private final ArrayList<Result> results = new ArrayList<>();
    public void setResults(List<Result> data) { results.clear(); if (data != null) results.addAll(data); }
    public ArrayList<Result> getAllResults() { return new ArrayList<>(results); }
    public void addResult(Result result) {
        for (Result r : results) {
            if (r.getStudent().getRollNo() == result.getStudent().getRollNo()
                    && r.getSubject().getSubjectId().equalsIgnoreCase(result.getSubject().getSubjectId())
                    && r.getExam().getExamId() == result.getExam().getExamId())
                throw new IllegalArgumentException("A result for this student, subject and exam already exists.");
        }
        results.add(result);
    }
    public void publishResult(Result result) {
        if (result == null || !results.contains(result))
            throw new IllegalArgumentException("Select a saved result to publish.");
        result.setPublished(true);
    }
    public ArrayList<Result> searchResults(int rollNo) {
        ArrayList<Result> found = new ArrayList<>();
        for (Result r : results) if (r.getStudent().getRollNo() == rollNo) found.add(r);
        return found;
    }
}
