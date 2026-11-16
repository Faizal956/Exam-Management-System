UNIVERSITY EXAMINATION MANAGEMENT SYSTEM
Java Swing desktop application

Requirements
- JDK 17 or later
- Windows PowerShell, Command Prompt, or Visual Studio Code with the Java extensions

Run from PowerShell
1. Open PowerShell in the ExamManagementSystem project folder.
2. Compile all Java source files as UTF-8:

   $sources = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
   javac -encoding UTF-8 -d out $sources

3. Start the application:

   java -cp out App

In Visual Studio Code, open the ExamManagementSystem folder, open src/App.java,
and run its main method. Stop any already-running copy before restarting to see
the latest UI changes.

Features
- University-style dashboard with live student, subject, upcoming examination,
  and published-result statistics
- Sidebar navigation, global module search, upcoming schedule, and quick actions
- Student registration, validation, search, update, and deletion
- Subject catalog with codes, credits, semesters, and maximum marks
- Examination scheduling with subject, date, time, type, maximum marks, and status
- Marks entry with automatic percentage, grade, and pass/fail calculations
- Draft and published result states
- Editable administrator display name saved across application restarts
- One-time demo-data action that appends examples without replacing records
- Sortable, searchable tables with record selection and confirmation dialogs
- Local persistence using Java serialization; no database or external library is required

Data files
The application reads and writes students.dat, subjects.dat, exams.dat, and
results.dat under data. When launched from the project folder, it reuses the
existing src/data files if they are present, so existing records are retained.
The same files are used when launching from src. Back up the data folder before
manually changing or removing saved records.
The administrator display name is stored separately in
app-settings.properties in the same data directory.
The supplied sample data includes one existing student plus three demonstration
students, subjects, upcoming exams, and results. Use Settings to append demo
records in a fresh data folder; the action is disabled when demo records exist.

All UI source is UTF-8. When compiling manually, include javac's
-encoding UTF-8 option as shown above.
