package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import managers.ExamManager;
import managers.ResultManager;
import managers.StudentManager;
import managers.SubjectManager;
import models.Exam;
import models.Result;
import models.Student;
import models.Subject;
import utils.AppSettings;
import utils.DemoData;
import utils.FileManager;

public class Dashboard extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Color NAVY = new Color(23, 37, 84);
    private static final Color NAVY_HOVER = new Color(34, 52, 109);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color ORANGE = new Color(245, 158, 11);
    private static final Color BACKGROUND = new Color(241, 245, 249);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final StudentManager students = new StudentManager();
    private final SubjectManager subjects = new SubjectManager();
    private final ExamManager exams = new ExamManager();
    private final ResultManager results = new ResultManager();
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageDeck = new JPanel(pageLayout);
    private final Map<String, JButton> navigation = new HashMap<>();
    private final Map<String, TableRowSorter<DefaultTableModel>> sorters = new HashMap<>();
    private final Map<String, JLabel> recordCounts = new HashMap<>();
    private final JLabel pageTitle = new JLabel("Dashboard");
    private final JLabel footerStatus = new JLabel("All changes are saved locally");
    private final JTextField globalSearch = new HintTextField("Search students, subjects, exams...");
    private final JLabel sidebarAdminName = new JLabel();
    private final JLabel sidebarAdminAvatar = new JLabel("", SwingConstants.CENTER);
    private final JLabel topbarAdminName = new JLabel();
    private final JLabel topbarAdminAvatar = new JLabel("AD", SwingConstants.CENTER);
    private String adminName = "Administrator";

    private final DefaultTableModel studentModel = tableModel(
        "Student ID", "Student name", "Email", "Course", "Semester", "Contact");
    private final DefaultTableModel subjectModel = tableModel(
        "Subject code", "Subject", "Credits", "Semester", "Maximum marks");
    private final DefaultTableModel examModel = tableModel(
        "Exam ID", "Examination", "Subject", "Date", "Time", "Type", "Max marks", "Status");
    private final DefaultTableModel resultModel = tableModel(
        "Student ID", "Student", "Subject", "Examination", "Marks", "Percent", "Grade", "Outcome", "Publication");

    private final JLabel studentMetric = new JLabel("0");
    private final JLabel subjectMetric = new JLabel("0");
    private final JLabel examMetric = new JLabel("0");
    private final JLabel resultMetric = new JLabel("0");
    private final DefaultTableModel scheduleModel = tableModel(
        "Examination", "Subject", "Date", "Time", "Status");

    private final JTextField studentId = input();
    private final JTextField studentName = input();
    private final JTextField studentEmail = input();
    private final JTextField studentCourse = input();
    private final JComboBox<Integer> studentSemester = semesterChoices();
    private final JTextField studentContact = input();

    private final JTextField subjectCode = input();
    private final JTextField subjectName = input();
    private final JComboBox<Integer> subjectCredits = new JComboBox<>(new Integer[] {1, 2, 3, 4, 5, 6});
    private final JComboBox<Integer> subjectSemester = semesterChoices();
    private final JTextField subjectMaxMarks = input();

    private final JTextField examId = input();
    private final JTextField examName = input();
    private final JComboBox<Subject> examSubject = new JComboBox<>();
    private final JTextField examDate = input();
    private final JTextField examTime = input();
    private final JComboBox<String> examType = new JComboBox<>(
        new String[] {"Midterm", "Final", "Quiz", "Practical", "Assignment"});
    private final JTextField examMaxMarks = input();
    private final JComboBox<String> examStatus = new JComboBox<>(
        new String[] {"Scheduled", "In Progress", "Completed", "Cancelled"});

    private final JComboBox<Student> resultStudent = new JComboBox<>();
    private final JComboBox<Subject> resultSubject = new JComboBox<>();
    private final JComboBox<Exam> resultExam = new JComboBox<>();
    private final JTextField resultMarks = input();
    private final JLabel resultPreview = new JLabel("Select a student and subject, then enter marks.");

    private JTable studentTable;
    private JTable subjectTable;
    private JTable examTable;
    private JTable resultTable;
    private String activePage = "Dashboard";

    public Dashboard() {
        super("University Examination Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1460, 920);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND);
        try {
            loadData();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Data loading error",
                JOptionPane.ERROR_MESSAGE);
        }
        buildInterface();
        refreshAll();
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(sidebar(), BorderLayout.WEST);

        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(BACKGROUND);
        workspace.add(topBar(), BorderLayout.NORTH);
        pageDeck.setBackground(BACKGROUND);
        pageDeck.add(dashboardPage(), "Dashboard");
        pageDeck.add(studentsPage(), "Students");
        pageDeck.add(subjectsPage(), "Subjects");
        pageDeck.add(examsPage(), "Examinations");
        pageDeck.add(resultsPage(), "Marks & Results");
        pageDeck.add(settingsPage(), "Settings");
        workspace.add(pageDeck, BorderLayout.CENTER);
        workspace.add(statusBar(), BorderLayout.SOUTH);
        root.add(workspace, BorderLayout.CENTER);
        setContentPane(root);
        showPage("Dashboard");
    }

    private JPanel sidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(NAVY);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 15, 20, 15));
        panel.setPreferredSize(new Dimension(252, 0));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 8, 29, 4));
        JLabel mark = new JLabel("U", SwingConstants.CENTER);
        mark.setOpaque(true);
        mark.setBackground(new Color(59, 130, 246));
        mark.setForeground(Color.WHITE);
        mark.setFont(new Font("Segoe UI", Font.BOLD, 20));
        mark.setPreferredSize(new Dimension(40, 40));
        mark.setBorder(BorderFactory.createLineBorder(new Color(96, 165, 250)));
        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 3));
        brandText.setOpaque(false);
        JLabel name = new JLabel("EXAM OFFICE");
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel descriptor = new JLabel("UNIVERSITY ERP");
        descriptor.setForeground(new Color(191, 207, 238));
        descriptor.setFont(new Font("Segoe UI", Font.BOLD, 9));
        brandText.add(name);
        brandText.add(descriptor);
        brand.add(mark, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        panel.add(brand);

        JLabel section = new JLabel("WORKSPACE");
        section.setForeground(new Color(153, 171, 210));
        section.setFont(new Font("Segoe UI", Font.BOLD, 10));
        section.setBorder(BorderFactory.createEmptyBorder(4, 12, 9, 0));
        section.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(section);
        addNavigation(panel, "Dashboard", IconType.DASHBOARD);
        addNavigation(panel, "Students", IconType.STUDENTS);
        addNavigation(panel, "Subjects", IconType.SUBJECTS);
        addNavigation(panel, "Examinations", IconType.EXAMS);
        addNavigation(panel, "Marks & Results", IconType.RESULTS);
        panel.add(Box.createVerticalGlue());
        JComponent divider = new JPanel();
        divider.setBackground(new Color(53, 70, 116));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(divider);
        panel.add(Box.createVerticalStrut(12));
        addNavigation(panel, "Settings", IconType.SETTINGS);

        RoundedPanel userCard = new RoundedPanel(new BorderLayout(10, 0), new Color(31, 48, 101), 14);
        userCard.setBorder(BorderFactory.createEmptyBorder(11, 10, 11, 10));
        JLabel avatar = sidebarAdminAvatar;
        avatar.setOpaque(true);
        avatar.setBackground(new Color(96, 165, 250));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        avatar.setPreferredSize(new Dimension(34, 34));
        JPanel user = new JPanel(new GridLayout(2, 1, 0, 3));
        user.setOpaque(false);
        JLabel admin = sidebarAdminName;
        admin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        admin.setForeground(Color.WHITE);
        JLabel role = new JLabel("Academic office");
        role.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        role.setForeground(new Color(191, 207, 238));
        user.add(admin);
        user.add(role);
        userCard.add(avatar, BorderLayout.WEST);
        userCard.add(user, BorderLayout.CENTER);
        userCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        panel.add(userCard);
        return panel;
    }

    private void addNavigation(JPanel sidebar, String title, IconType icon) {
        JButton button = new JButton(title);
        button.setUI(new BasicButtonUI());
        button.setIcon(new AppIcon(icon, 19, Color.WHITE));
        button.setIconTextGap(14);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(new Color(211, 220, 239));
        button.setBackground(NAVY);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(11, 13, 11, 10));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> showPage(title));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!title.equals(activePage)) button.setBackground(NAVY_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!title.equals(activePage)) button.setBackground(NAVY);
            }
        });
        navigation.put(title, button);
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(4));
    }

    private JPanel topBar() {
        JPanel bar = new JPanel(new BorderLayout(18, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(16, 28, 16, 28)));
        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBlock.setOpaque(false);
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 21));
        pageTitle.setForeground(TEXT);
        JLabel breadcrumb = new JLabel("UNIVERSITY  /  ACADEMIC MANAGEMENT");
        breadcrumb.setFont(new Font("Segoe UI", Font.BOLD, 9));
        breadcrumb.setForeground(MUTED);
        titleBlock.add(pageTitle);
        titleBlock.add(breadcrumb);
        bar.add(titleBlock, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 13, 0));
        right.setOpaque(false);
        globalSearch.setColumns(22);
        globalSearch.setFont(BODY);
        globalSearch.setForeground(TEXT);
        globalSearch.setToolTipText("Search the current module");
        globalSearch.setPreferredSize(new Dimension(250, 38));
        globalSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        globalSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                if (!globalSearch.getText().isEmpty() && "Dashboard".equals(activePage)) {
                    showPage("Students");
                }
                applySearch();
            }
            @Override public void insertUpdate(DocumentEvent e) { changed(); }
            @Override public void removeUpdate(DocumentEvent e) { changed(); }
            @Override public void changedUpdate(DocumentEvent e) { changed(); }
        });
        right.add(globalSearch);
        JButton notifications = new JButton(new AppIcon(IconType.BELL, 18, MUTED));
        styleIconButton(notifications);
        notifications.setToolTipText("Notifications");
        notifications.addActionListener(e -> showNotice());
        right.add(notifications);
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d")));
        date.setFont(new Font("Segoe UI", Font.BOLD, 12));
        date.setForeground(MUTED);
        right.add(date);
        JLabel profile = topbarAdminAvatar;
        profile.setOpaque(true);
        profile.setBackground(new Color(219, 234, 254));
        profile.setForeground(new Color(30, 64, 175));
        profile.setFont(new Font("Segoe UI", Font.BOLD, 11));
        profile.setPreferredSize(new Dimension(34, 34));
        JLabel profileName = topbarAdminName;
        profileName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        profileName.setForeground(TEXT);
        JPanel profileBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        profileBlock.setOpaque(false);
        profileBlock.add(profile);
        profileBlock.add(profileName);
        right.add(profileBlock);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel dashboardPage() {
        JPanel content = pageCanvas();
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        RoundedPanel welcome = new RoundedPanel(new BorderLayout(18, 0), new Color(30, 64, 175), 20);
        welcome.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        JPanel welcomeText = new JPanel(new GridLayout(2, 1, 0, 7));
        welcomeText.setOpaque(false);
        JLabel greeting = new JLabel("Welcome Back, Admin!");
        greeting.setForeground(Color.WHITE);
        greeting.setFont(new Font("Segoe UI", Font.BOLD, 25));
        JLabel welcomeSubtitle = new JLabel("Manage your academic activities efficiently.");
        welcomeSubtitle.setForeground(new Color(219, 234, 254));
        welcomeSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeText.add(greeting);
        welcomeText.add(welcomeSubtitle);
        JLabel day = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        day.setForeground(Color.WHITE);
        day.setFont(new Font("Segoe UI", Font.BOLD, 12));
        welcome.add(welcomeText, BorderLayout.WEST);
        welcome.add(day, BorderLayout.EAST);
        welcome.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        body.add(welcome);
        body.add(Box.createVerticalStrut(20));

        JPanel metrics = new JPanel(new GridLayout(1, 4, 16, 0));
        metrics.setOpaque(false);
        metrics.add(metricCard("TOTAL STUDENTS", studentMetric, "Enrolled learners", IconType.STUDENTS, BLUE));
        metrics.add(metricCard("TOTAL SUBJECTS", subjectMetric, "Active curriculum", IconType.SUBJECTS, GREEN));
        metrics.add(metricCard("UPCOMING EXAMS", examMetric, "Scheduled ahead", IconType.EXAMS, ORANGE));
        metrics.add(metricCard("RESULTS PUBLISHED", resultMetric, "Results released to students", IconType.RESULTS, new Color(139, 92, 246)));
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        body.add(metrics);
        body.add(Box.createVerticalStrut(20));

        JPanel bottom = new JPanel(new GridLayout(1, 2, 18, 0));
        bottom.setOpaque(false);
        bottom.add(scheduleCard());
        bottom.add(quickActionsCard());
        bottom.setPreferredSize(new Dimension(900, 330));
        bottom.setMinimumSize(new Dimension(500, 300));
        bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 370));
        body.add(bottom);
        body.add(Box.createVerticalGlue());

        content.add(body, BorderLayout.NORTH);
        return scrollPage(content);
    }

    private RoundedPanel metricCard(String label, JLabel value, String detail,
            IconType icon, Color color) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(8, 12), Color.WHITE, 17);
        card.setBorder(BorderFactory.createEmptyBorder(17, 18, 15, 18));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 10));
        title.setForeground(MUTED);
        JLabel symbol = new JLabel(new AppIcon(icon, 20, color));
        top.add(title, BorderLayout.WEST);
        top.add(symbol, BorderLayout.EAST);
        value.setFont(new Font("Segoe UI", Font.BOLD, 29));
        value.setForeground(TEXT);
        JLabel caption = new JLabel(detail);
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        caption.setForeground(MUTED);
        JPanel numbers = new JPanel(new GridLayout(2, 1, 0, 3));
        numbers.setOpaque(false);
        numbers.add(value);
        numbers.add(caption);
        card.add(top, BorderLayout.NORTH);
        card.add(numbers, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        return card;
    }

    private RoundedPanel scheduleCard() {
        RoundedPanel card = sectionCard("Upcoming examination schedule",
            "The next scheduled assessments");
        JTable table = createTable(scheduleModel);
        table.setRowHeight(35);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        card.add(tablePanel(table), BorderLayout.CENTER);
        JButton viewAll = textButton("View examinations", false);
        viewAll.addActionListener(e -> showPage("Examinations"));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(viewAll);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private RoundedPanel quickActionsCard() {
        RoundedPanel card = sectionCard("Quick actions", "Common academic office tasks");
        JPanel actions = new JPanel(new GridLayout(2, 2, 12, 12));
        actions.setOpaque(false);
        actions.add(quickAction("Register student", "Create a student record", IconType.STUDENTS, BLUE, "Students"));
        actions.add(quickAction("Add subject", "Update the curriculum", IconType.SUBJECTS, GREEN, "Subjects"));
        actions.add(quickAction("Schedule examination", "Plan an assessment", IconType.EXAMS, ORANGE, "Examinations"));
        actions.add(quickAction("Enter marks", "Record student results", IconType.RESULTS,
            new Color(139, 92, 246), "Marks & Results"));
        card.add(actions, BorderLayout.CENTER);
        return card;
    }

    private JButton quickAction(String title, String subtitle, IconType icon, Color color, String target) {
        JButton action = new JButton();
        action.setUI(new BasicButtonUI());
        action.setLayout(new BorderLayout(11, 0));
        action.setBackground(new Color(248, 250, 252));
        action.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        action.setFocusPainted(false);
        action.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel glyph = new JLabel(new AppIcon(icon, 22, color));
        JPanel labels = new JPanel(new GridLayout(2, 1, 0, 5));
        labels.setOpaque(false);
        JLabel main = new JLabel(title);
        main.setFont(new Font("Segoe UI", Font.BOLD, 12));
        main.setForeground(TEXT);
        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sub.setForeground(MUTED);
        labels.add(main);
        labels.add(sub);
        action.add(glyph, BorderLayout.WEST);
        action.add(labels, BorderLayout.CENTER);
        action.addActionListener(e -> showPageForQuickAction(target));
        action.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                action.setBackground(new Color(239, 246, 255));
                action.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(191, 219, 254)),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)));
            }
            @Override public void mouseExited(MouseEvent e) {
                action.setBackground(new Color(248, 250, 252));
                action.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)));
            }
        });
        return action;
    }

    private JPanel studentsPage() {
        studentTable = createTable(studentModel);
        registerSorter("Students", studentTable);
        JPanel form = new JPanel(new GridBagLayout());
        addField(form, "Student ID", studentId, 0, 0);
        addField(form, "Full name", studentName, 1, 0);
        addField(form, "Email address", studentEmail, 2, 0);
        addField(form, "Course / program", studentCourse, 0, 1);
        addField(form, "Semester", studentSemester, 1, 1);
        addField(form, "Contact number", studentContact, 2, 1);
        JPanel actions = actionRow();
        addActionRow(form, actions, 3, 2);
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentTable.getSelectedRow() >= 0) fillStudentForm();
        });
        JButton add = button("Register student", true);
        JButton update = button("Save changes", false);
        JButton delete = button("Delete", false);
        JButton clear = button("Clear", false);
        actions.add(add); actions.add(update); actions.add(delete); actions.add(clear);
        add.addActionListener(e -> runAction(() -> {
            Student student = new Student(integer(studentId, "Student ID"), required(studentName, "Name"),
                required(studentEmail, "Email"), required(studentCourse, "Course"),
                selected(studentSemester, "Semester"), studentContact.getText().trim());
            students.addStudent(student);
            FileManager.save("students.dat", students.getAllStudents());
            clearStudentForm();
            refreshAll();
            notice("Student registered successfully.");
        }));
        update.addActionListener(e -> runAction(() -> {
            students.updateStudent(integer(studentId, "Student ID"), required(studentName, "Name"),
                required(studentEmail, "Email"), required(studentCourse, "Course"),
                selected(studentSemester, "Semester"), studentContact.getText().trim());
            FileManager.save("students.dat", students.getAllStudents());
            refreshAll();
            notice("Student record updated.");
        }));
        delete.addActionListener(e -> runAction(() -> {
            int id = integer(studentId, "Student ID");
            if (!confirm("Delete student " + id + "?")) return;
            if (!students.deleteStudent(id)) throw new IllegalArgumentException("Student not found.");
            FileManager.save("students.dat", students.getAllStudents());
            clearStudentForm();
            refreshAll();
            notice("Student record deleted.");
        }));
        clear.addActionListener(e -> clearStudentForm());
        return modulePage("Student directory", "Register and maintain student profiles.",
            form, studentTable, studentModel.getRowCount());
    }

    private JPanel subjectsPage() {
        subjectTable = createTable(subjectModel);
        registerSorter("Subjects", subjectTable);
        JPanel form = new JPanel(new GridBagLayout());
        addField(form, "Subject code", subjectCode, 0, 0);
        addField(form, "Subject name", subjectName, 1, 0);
        addField(form, "Credits", subjectCredits, 2, 0);
        addField(form, "Semester", subjectSemester, 0, 1);
        addField(form, "Maximum marks", subjectMaxMarks, 1, 1);
        JPanel actions = actionRow();
        addActionRow(form, actions, 2, 1);
        subjectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && subjectTable.getSelectedRow() >= 0) fillSubjectForm();
        });
        JButton add = button("Add subject", true);
        JButton update = button("Save changes", false);
        JButton delete = button("Delete", false);
        JButton clear = button("Clear", false);
        actions.add(add); actions.add(update); actions.add(delete); actions.add(clear);
        add.addActionListener(e -> runAction(() -> {
            Subject subject = new Subject(required(subjectCode, "Subject code"),
                required(subjectName, "Subject name"), positiveInteger(subjectMaxMarks, "Maximum marks"),
                selected(subjectCredits, "Credits"), selected(subjectSemester, "Semester"));
            subjects.addSubject(subject);
            FileManager.save("subjects.dat", subjects.getAllSubjects());
            clearSubjectForm();
            refreshAll();
            notice("Subject added to the catalog.");
        }));
        update.addActionListener(e -> runAction(() -> {
            subjects.updateSubject(required(subjectCode, "Subject code"),
                required(subjectName, "Subject name"), positiveInteger(subjectMaxMarks, "Maximum marks"),
                selected(subjectCredits, "Credits"), selected(subjectSemester, "Semester"));
            FileManager.save("subjects.dat", subjects.getAllSubjects());
            refreshAll();
            notice("Subject updated.");
        }));
        delete.addActionListener(e -> runAction(() -> {
            String code = required(subjectCode, "Subject code");
            if (!confirm("Delete subject " + code + "?")) return;
            if (!subjects.deleteSubject(code)) throw new IllegalArgumentException("Subject not found.");
            FileManager.save("subjects.dat", subjects.getAllSubjects());
            clearSubjectForm();
            refreshAll();
            notice("Subject deleted.");
        }));
        clear.addActionListener(e -> clearSubjectForm());
        return modulePage("Subject catalog", "Maintain course subjects, credits, and assessment totals.",
            form, subjectTable, subjectModel.getRowCount());
    }

    private JPanel examsPage() {
        examTable = createTable(examModel);
        registerSorter("Examinations", examTable);
        examTable.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        JPanel form = new JPanel(new GridBagLayout());
        addField(form, "Examination ID", examId, 0, 0);
        addField(form, "Examination name", examName, 1, 0);
        addField(form, "Subject", examSubject, 2, 0);
        addField(form, "Date (YYYY-MM-DD)", examDate, 0, 1);
        addField(form, "Time (HH:MM)", examTime, 1, 1);
        addField(form, "Examination type", examType, 2, 1);
        addField(form, "Maximum marks", examMaxMarks, 0, 2);
        addField(form, "Status", examStatus, 1, 2);
        JPanel actions = actionRow();
        addActionRow(form, actions, 2, 2);
        examTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && examTable.getSelectedRow() >= 0) fillExamForm();
        });
        JButton add = button("Schedule examination", true);
        JButton update = button("Save changes", false);
        JButton delete = button("Delete", false);
        JButton clear = button("Clear", false);
        actions.add(add); actions.add(update); actions.add(delete); actions.add(clear);
        examSubject.addActionListener(e -> {
            Subject subject = (Subject) examSubject.getSelectedItem();
            if (subject != null) examMaxMarks.setText(String.valueOf(subject.getMaxMarks()));
        });
        add.addActionListener(e -> runAction(() -> saveExam(false)));
        update.addActionListener(e -> runAction(() -> saveExam(true)));
        delete.addActionListener(e -> runAction(() -> {
            int id = integer(examId, "Examination ID");
            if (!confirm("Delete examination " + id + "?")) return;
            if (!exams.deleteExam(id)) throw new IllegalArgumentException("Examination not found.");
            FileManager.save("exams.dat", exams.getAllExams());
            clearExamForm();
            refreshAll();
            notice("Examination deleted.");
        }));
        clear.addActionListener(e -> clearExamForm());
        return modulePage("Examination calendar", "Schedule and manage university assessments.",
            form, examTable, examModel.getRowCount());
    }

    private JPanel resultsPage() {
        resultTable = createTable(resultModel);
        registerSorter("Marks & Results", resultTable);
        resultTable.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        resultTable.getColumnModel().getColumn(8).setCellRenderer(new StatusRenderer());
        JPanel form = new JPanel(new GridBagLayout());
        addField(form, "Student", resultStudent, 0, 0);
        addField(form, "Subject", resultSubject, 1, 0);
        addField(form, "Examination", resultExam, 2, 0);
        addField(form, "Marks obtained", resultMarks, 0, 1);
        resultPreview.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resultPreview.setForeground(BLUE);
        addField(form, "Calculated result", resultPreview, 1, 1);
        JPanel actions = actionRow();
        addActionRow(form, actions, 2, 1);
        resultStudent.setRenderer(new ModelRenderer<Student>() {
            @Override protected String text(Student student) {
                return student == null ? "" : student.getRollNo() + "  -  " + student.getName();
            }
        });
        resultSubject.setRenderer(new ModelRenderer<Subject>() {
            @Override protected String text(Subject subject) {
                return subject == null ? "" : subject.getSubjectId() + "  -  " + subject.getSubjectName();
            }
        });
        resultExam.setRenderer(new ModelRenderer<Exam>() {
            @Override protected String text(Exam exam) {
                return exam == null ? "" : exam.getExamId() + "  -  " + exam.getExamName();
            }
        });
        resultExam.addActionListener(e -> {
            Exam exam = (Exam) resultExam.getSelectedItem();
            if (exam != null && !exam.getSubjectId().isEmpty()) {
                Subject linkedSubject = subjects.searchSubject(exam.getSubjectId());
                if (linkedSubject != null) resultSubject.setSelectedItem(linkedSubject);
            }
            updateResultPreview();
        });
        JButton save = button("Save result", true);
        JButton publish = button("Publish selected", false);
        JButton clear = button("Clear", false);
        actions.add(save); actions.add(publish); actions.add(clear);
        save.addActionListener(e -> runAction(() -> {
            Student student = selected(resultStudent, "Student");
            Subject subject = selected(resultSubject, "Subject");
            Exam exam = selected(resultExam, "Examination");
            if (!exam.getSubjectId().isEmpty()
                    && !exam.getSubjectId().equalsIgnoreCase(subject.getSubjectId())) {
                throw new IllegalArgumentException("The selected examination is scheduled for a different subject.");
            }
            Result result = new Result(student, subject, exam,
                decimal(resultMarks, "Marks obtained"));
            results.addResult(result);
            FileManager.save("results.dat", results.getAllResults());
            resultMarks.setText("");
            refreshAll();
            notice("Marks saved. Grade: " + result.calculateGrade()
                + (result.isPassing() ? " (Pass)" : " (Fail)"));
        }));
        publish.addActionListener(e -> runAction(() -> {
            int row = resultTable.getSelectedRow();
            if (row < 0) throw new IllegalArgumentException("Select a saved result from the table first.");
            int modelRow = resultTable.convertRowIndexToModel(row);
            Result result = results.getAllResults().get(modelRow);
            if (result.isPublished()) throw new IllegalArgumentException("This result is already published.");
            if (!confirm("Publish the result for " + result.getStudent().getName() + "?")) return;
            results.publishResult(result);
            FileManager.save("results.dat", results.getAllResults());
            refreshAll();
            notice("Result published.");
        }));
        clear.addActionListener(e -> {
            resultMarks.setText("");
            resultPreview.setText("Select a student and subject, then enter marks.");
        });
        resultMarks.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateResultPreview(); }
            @Override public void removeUpdate(DocumentEvent e) { updateResultPreview(); }
            @Override public void changedUpdate(DocumentEvent e) { updateResultPreview(); }
        });
        resultSubject.addActionListener(e -> updateResultPreview());
        return modulePage("Marks & results", "Enter marks and review grades and pass status.",
            form, resultTable, resultModel.getRowCount());
    }

    private JPanel settingsPage() {
        JPanel content = pageCanvas();
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.add(pageHeading("Settings", "Application preferences and local data storage."));
        column.add(Box.createVerticalStrut(18));
        RoundedPanel demo = sectionCard("Sample academic records",
            "Add example students, subjects, upcoming exams, and results for a demonstration.");
        JLabel demoDescription = new JLabel("<html><div style='width:570px'>"
            + "Demo data is added alongside your existing records. It will not overwrite "
            + "or remove any of your data, and the action can only be applied once."
            + "</div></html>");
        demoDescription.setFont(BODY);
        demoDescription.setForeground(TEXT);
        demo.add(demoDescription, BorderLayout.CENTER);
        boolean demoDataPresent = DemoData.isPresent(students, subjects, exams);
        JButton addDemo = button(demoDataPresent ? "Demo data loaded" : "Add demo data", !demoDataPresent);
        addDemo.setEnabled(!demoDataPresent);
        addDemo.addActionListener(e -> runAction(() -> {
            if (!confirm("Add sample students, subjects, upcoming examinations, and results? "
                    + "Your existing records will be kept.")) return;
            DemoData.addTo(students, subjects, exams, results);
            refreshAll();
            addDemo.setText("Demo data loaded");
            addDemo.setEnabled(false);
            notice("Demo data added. Existing records were kept.");
        }));
        JPanel demoActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        demoActions.setOpaque(false);
        demoActions.add(addDemo);
        demo.add(demoActions, BorderLayout.SOUTH);
        demo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 205));
        column.add(demo);
        column.add(Box.createVerticalStrut(16));

        RoundedPanel storage = sectionCard("Local data storage", "Your records are saved on this computer.");
        JLabel path = new JLabel("<html><div style='width:550px'>"
            + escapeHtml(FileManager.getDataDirectory().toAbsolutePath().toString())
            + "</div></html>");
        path.setFont(BODY);
        path.setForeground(TEXT);
        storage.add(path, BorderLayout.CENTER);
        JButton reload = button("Reload saved data", false);
        reload.addActionListener(e -> runAction(() -> {
            if (!confirm("Reload records from disk? Unsaved changes will be discarded.")) return;
            loadData();
            refreshAll();
            notice("Saved records reloaded.");
        }));
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(reload);
        storage.add(buttonRow, BorderLayout.SOUTH);
        storage.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        column.add(storage);

        RoundedPanel administrator = sectionCard("Administrator profile",
            "Choose the name displayed in the sidebar and top navigation.");
        JTextField nameField = input();
        nameField.setText(adminName);
        JPanel nameForm = new JPanel(new GridBagLayout());
        addField(nameForm, "Administrator name", nameField, 0, 0);
        JButton saveName = button("Save name", true);
        JPanel profileActions = actionRow();
        profileActions.add(saveName);
        addActionRow(nameForm, profileActions, 1, 0);
        styleForm(nameForm);
        administrator.add(nameForm, BorderLayout.CENTER);
        saveName.addActionListener(e -> runAction(() -> {
            String updatedName = required(nameField, "Administrator name");
            AppSettings.saveAdminName(updatedName);
            adminName = updatedName.trim();
            updateAdminProfile();
            nameField.setText(adminName);
            notice("Administrator name saved.");
        }));
        administrator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        column.add(Box.createVerticalStrut(16));
        column.add(administrator);

        content.add(column, BorderLayout.NORTH);
        return scrollPage(content);
    }

    private JPanel modulePage(String title, String subtitle, JPanel form, JTable table, int rowCount) {
        JPanel content = pageCanvas();
        JPanel column = new JPanel(new BorderLayout(0, 16));
        column.setOpaque(false);
        column.add(pageHeading(title, subtitle), BorderLayout.NORTH);

        RoundedPanel formCard = new RoundedPanel(new BorderLayout(), Color.WHITE, 18);
        formCard.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        styleForm(form);
        formCard.add(form, BorderLayout.CENTER);
        JPanel tableCard = new RoundedPanel(new BorderLayout(0, 12), Color.WHITE, 18);
        tableCard.setBorder(BorderFactory.createEmptyBorder(17, 18, 15, 18));
        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setOpaque(false);
        JLabel listTitle = new JLabel("Records");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        listTitle.setForeground(TEXT);
        JLabel count = new JLabel(rowCount + " records");
        count.setFont(BODY);
        count.setForeground(MUTED);
        recordCounts.put(titleForPage(title), count);
        tableHeading.add(listTitle, BorderLayout.WEST);
        tableHeading.add(count, BorderLayout.EAST);
        tableCard.add(tableHeading, BorderLayout.NORTH);
        tableCard.add(tablePanel(table), BorderLayout.CENTER);
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(formCard, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        column.add(body, BorderLayout.CENTER);
        content.add(column, BorderLayout.CENTER);
        return content;
    }

    private JPanel pageHeading(String title, String subtitle) {
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 5));
        heading.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        titleLabel.setForeground(TEXT);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(BODY);
        subtitleLabel.setForeground(MUTED);
        heading.add(titleLabel);
        heading.add(subtitleLabel);
        return heading;
    }

    private RoundedPanel sectionCard(String title, String subtitle) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 13), Color.WHITE, 18);
        card.setBorder(BorderFactory.createEmptyBorder(17, 18, 14, 18));
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 4));
        heading.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(TEXT);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(MUTED);
        heading.add(titleLabel);
        heading.add(subtitleLabel);
        card.add(heading, BorderLayout.NORTH);
        return card;
    }

    private JPanel pageCanvas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 27, 24, 27));
        return panel;
    }

    private JPanel scrollPage(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel tablePanel(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(BODY);
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 37));
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(MUTED);
        header.setBackground(new Color(248, 250, 252));
        header.setReorderingAllowed(false);
        return table;
    }

    private void registerSorter(String page, JTable table) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(studentModel);
        if ("Subjects".equals(page)) sorter = new TableRowSorter<>(subjectModel);
        if ("Examinations".equals(page)) sorter = new TableRowSorter<>(examModel);
        if ("Marks & Results".equals(page)) sorter = new TableRowSorter<>(resultModel);
        table.setRowSorter(sorter);
        sorters.put(page, sorter);
    }

    private void applySearch() {
        TableRowSorter<DefaultTableModel> sorter = sorters.get(activePage);
        if (sorter == null) return;
        String query = globalSearch.getText().trim();
        sorter.setRowFilter(query.isEmpty() ? null
            : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
    }

    private JPanel statusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
            BorderFactory.createEmptyBorder(9, 20, 9, 20)));
        footerStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerStatus.setForeground(MUTED);
        JLabel environment = new JLabel("UNIVERSITY  ·  EXAM MANAGEMENT");
        environment.setFont(new Font("Segoe UI", Font.BOLD, 9));
        environment.setForeground(MUTED);
        bar.add(footerStatus, BorderLayout.WEST);
        bar.add(environment, BorderLayout.EAST);
        return bar;
    }

    private void addField(JPanel panel, String label, JComponent input, int column, int row) {
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        JLabel caption = new JLabel(label);
        caption.setFont(LABEL);
        caption.setForeground(new Color(71, 85, 105));
        group.add(caption, BorderLayout.NORTH);
        group.add(input, BorderLayout.CENTER);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = column;
        c.gridy = row;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(5, 6, 8, 6);
        panel.add(group, c);
    }

    private void addActionRow(JPanel form, JPanel actions, int column, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = column;
        c.gridy = row;
        c.gridwidth = 3 - column;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 5, 5, 5);
        form.add(actions, c);
    }

    private JPanel actionRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        panel.setOpaque(false);
        return panel;
    }

    private void styleForm(Component component) {
        if (component instanceof JPanel) {
            ((JPanel) component).setBackground(Color.WHITE);
        } else if (component instanceof JTextField) {
            JTextField field = (JTextField) component;
            field.setFont(BODY);
            field.setForeground(TEXT);
            field.setBackground(Color.WHITE);
            field.setCaretColor(BLUE);
            field.setPreferredSize(new Dimension(Math.max(100, field.getPreferredSize().width), 36));
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        } else if (component instanceof JComboBox<?>) {
            JComboBox<?> combo = (JComboBox<?>) component;
            combo.setFont(BODY);
            combo.setForeground(TEXT);
            combo.setBackground(Color.WHITE);
            combo.setPreferredSize(new Dimension(Math.max(110, combo.getPreferredSize().width), 36));
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) styleForm(child);
        }
    }

    private JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        Color normal = primary ? BLUE : new Color(248, 250, 252);
        Color foreground = primary ? Color.WHITE : new Color(51, 65, 85);
        Color outline = primary ? BLUE : BORDER;
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(normal);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(outline),
            BorderFactory.createEmptyBorder(8, 13, 8, 13)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                button.setBackground(primary ? new Color(29, 78, 216) : new Color(241, 245, 249));
            }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(normal); }
        });
        return button;
    }

    private JButton textButton(String text, boolean primary) {
        return button(text, primary);
    }

    private void styleIconButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setPreferredSize(new Dimension(38, 38));
        button.setBackground(new Color(248, 250, 252));
        button.setBorder(BorderFactory.createLineBorder(BORDER));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JTextField input() {
        JTextField field = new JTextField();
        field.setFont(BODY);
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);
        field.setCaretColor(BLUE);
        field.setPreferredSize(new Dimension(150, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        return field;
    }

    private JComboBox<Integer> semesterChoices() {
        return new JComboBox<>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    }

    private static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private void loadData() {
        students.setStudents(FileManager.load("students.dat"));
        subjects.setSubjects(FileManager.load("subjects.dat"));
        exams.setExams(FileManager.load("exams.dat"));
        results.setResults(FileManager.load("results.dat"));
        adminName = AppSettings.loadAdminName();
        updateAdminProfile();
    }

    private void updateAdminProfile() {
        if (sidebarAdminName == null) return;
        sidebarAdminName.setText(adminName);
        sidebarAdminAvatar.setText(initials(adminName));
        topbarAdminName.setText(adminName);
        topbarAdminAvatar.setText(initials(adminName));
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder(2);
        for (String part : parts) {
            if (!part.isEmpty()) initials.append(Character.toUpperCase(part.charAt(0)));
            if (initials.length() == 2) break;
        }
        return initials.length() == 0 ? "A" : initials.toString();
    }

    private void refreshAll() {
        refreshStudents();
        refreshSubjects();
        refreshExams();
        refreshResults();
        refreshCombos();
        refreshDashboard();
        refreshRecordCounts();
    }

    private void refreshStudents() {
        studentModel.setRowCount(0);
        for (Student student : students.getAllStudents()) {
            studentModel.addRow(new Object[] {student.getRollNo(), student.getName(),
                student.getEmail(), student.getCourse(), student.getSemester(),
                student.getContactNumber()});
        }
    }

    private void refreshSubjects() {
        subjectModel.setRowCount(0);
        for (Subject subject : subjects.getAllSubjects()) {
            subjectModel.addRow(new Object[] {subject.getSubjectId(), subject.getSubjectName(),
                subject.getCredits(), subject.getSemester(), subject.getMaxMarks()});
        }
    }

    private void refreshExams() {
        examModel.setRowCount(0);
        for (Exam exam : exams.getAllExams()) {
            Subject subject = subjects.searchSubject(exam.getSubjectId());
            int maxMarks = exam.getMaxMarks() > 0 ? exam.getMaxMarks()
                : subject == null ? 100 : subject.getMaxMarks();
            examModel.addRow(new Object[] {exam.getExamId(), exam.getExamName(),
                subject == null ? exam.getSubjectId() : subject.getSubjectName(),
                exam.getExamDate(), exam.getExamTime(), exam.getExamType(),
                maxMarks, exam.getExamStatus()});
        }
    }

    private void refreshResults() {
        resultModel.setRowCount(0);
        for (Result result : results.getAllResults()) {
            resultModel.addRow(new Object[] {result.getStudent().getRollNo(),
                result.getStudent().getName(), result.getSubject().getSubjectName(),
                result.getExam().getExamName(), result.getMarksObtained(),
                String.format("%.1f%%", result.calculatePercentage()),
                result.calculateGrade(), result.isPassing() ? "PASS" : "FAIL",
                result.isPublished() ? "Published" : "Draft"});
        }
    }

    private void refreshCombos() {
        setComboItems(examSubject, subjects.getAllSubjects());
        setComboItems(resultStudent, students.getAllStudents());
        setComboItems(resultSubject, subjects.getAllSubjects());
        setComboItems(resultExam, exams.getAllExams());
        setRenderer(examSubject, new ModelRenderer<Subject>() {
            @Override protected String text(Subject subject) {
                return subject == null ? "Choose a subject" : subject.getSubjectId()
                    + " - " + subject.getSubjectName();
            }
        });
    }

    private void refreshDashboard() {
        studentMetric.setText(String.valueOf(students.getAllStudents().size()));
        subjectMetric.setText(String.valueOf(subjects.getAllSubjects().size()));
        List<Exam> upcoming = upcomingExams();
        examMetric.setText(String.valueOf(upcoming.size()));
        long publishedCount = results.getAllResults().stream().filter(Result::isPublished).count();
        resultMetric.setText(String.valueOf(publishedCount));
        scheduleModel.setRowCount(0);
        for (Exam exam : upcoming.subList(0, Math.min(5, upcoming.size()))) {
            Subject subject = subjects.searchSubject(exam.getSubjectId());
            scheduleModel.addRow(new Object[] {exam.getExamName(),
                subject == null ? (exam.getSubjectId().isEmpty() ? "—" : exam.getSubjectId())
                    : subject.getSubjectName(),
                exam.getExamDate(), exam.getExamTime(), exam.getExamStatus()});
        }
        if (upcoming.isEmpty()) {
            scheduleModel.addRow(new Object[] {"No upcoming examinations", "—", "—", "—", "—"});
        }
    }

    private List<Exam> upcomingExams() {
        ArrayList<Exam> upcoming = new ArrayList<>();
        for (Exam exam : exams.getAllExams()) {
            if (!"Scheduled".equalsIgnoreCase(exam.getExamStatus())) continue;
            try {
                if (!LocalDate.parse(exam.getExamDate(), DATE).isBefore(LocalDate.now())) upcoming.add(exam);
            } catch (DateTimeParseException ignored) {
                // Legacy records with malformed dates remain visible in the examination list.
            }
        }
        upcoming.sort(Comparator.comparing(Exam::getExamDate).thenComparing(Exam::getExamTime));
        return upcoming;
    }

    private void refreshRecordCounts() {
        updateRecordCount("Students", studentModel.getRowCount());
        updateRecordCount("Subjects", subjectModel.getRowCount());
        updateRecordCount("Examinations", examModel.getRowCount());
        updateRecordCount("Marks & Results", resultModel.getRowCount());
    }

    private void updateRecordCount(String page, int count) {
        JLabel label = recordCounts.get(page);
        if (label != null) label.setText(count + (count == 1 ? " record" : " records"));
    }

    private String titleForPage(String title) {
        if ("Student directory".equals(title)) return "Students";
        if ("Subject catalog".equals(title)) return "Subjects";
        if ("Examination calendar".equals(title)) return "Examinations";
        return "Marks & Results";
    }

    private void showPage(String page) {
        activePage = page;
        pageTitle.setText(page.equals("Examinations") ? "Examinations" : page);
        pageLayout.show(pageDeck, page);
        for (Map.Entry<String, JButton> item : navigation.entrySet()) {
            boolean active = item.getKey().equals(page);
            JButton button = item.getValue();
            button.setBackground(active ? new Color(37, 57, 118) : NAVY);
            button.setForeground(active ? Color.WHITE : new Color(211, 220, 239));
            button.setBorder(active
                ? BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(96, 165, 250)),
                    BorderFactory.createEmptyBorder(11, 10, 11, 10))
                : BorderFactory.createEmptyBorder(11, 13, 11, 10));
        }
        applySearch();
    }

    private void saveExam(boolean update) {
        int id = integer(examId, "Examination ID");
        Subject subject = selected(examSubject, "Subject");
        Exam exam = new Exam(id, required(examName, "Examination name"),
            validDate(examDate), validTime(examTime), selected(examType, "Examination type"),
            selected(examStatus, "Status"), subject.getSubjectId(),
            positiveInteger(examMaxMarks, "Maximum marks"));
        if (update) {
            exams.updateExam(id, exam.getExamName(), exam.getExamDate(), exam.getExamTime(),
                exam.getExamType(), exam.getExamStatus(), exam.getSubjectId(), exam.getMaxMarks());
        } else {
            exams.addExam(exam);
        }
        FileManager.save("exams.dat", exams.getAllExams());
        clearExamForm();
        refreshAll();
        notice(update ? "Examination updated." : "Examination scheduled.");
    }

    private String validDate(JTextField field) {
        String value = required(field, "Date");
        try {
            return LocalDate.parse(value, DATE).format(DATE);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Enter the date in YYYY-MM-DD format.");
        }
    }

    private String validTime(JTextField field) {
        String value = required(field, "Time");
        try {
            return LocalTime.parse(value, TIME).format(TIME);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Enter the time in 24-hour HH:MM format.");
        }
    }

    private void fillStudentForm() {
        int row = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());
        Student student = students.searchStudent(Integer.parseInt(studentModel.getValueAt(row, 0).toString()));
        if (student == null) return;
        studentId.setText(String.valueOf(student.getRollNo()));
        studentName.setText(student.getName());
        studentEmail.setText(student.getEmail());
        studentCourse.setText(student.getCourse());
        studentSemester.setSelectedItem(student.getSemester());
        studentContact.setText(student.getContactNumber());
    }

    private void fillSubjectForm() {
        int row = subjectTable.convertRowIndexToModel(subjectTable.getSelectedRow());
        Subject subject = subjects.searchSubject(subjectModel.getValueAt(row, 0).toString());
        if (subject == null) return;
        subjectCode.setText(subject.getSubjectId());
        subjectName.setText(subject.getSubjectName());
        subjectCredits.setSelectedItem(subject.getCredits() > 0 ? subject.getCredits() : 1);
        subjectSemester.setSelectedItem(subject.getSemester() > 0 ? subject.getSemester() : 1);
        subjectMaxMarks.setText(String.valueOf(subject.getMaxMarks()));
    }

    private void fillExamForm() {
        int row = examTable.convertRowIndexToModel(examTable.getSelectedRow());
        int id = Integer.parseInt(examModel.getValueAt(row, 0).toString());
        Exam exam = exams.searchExam(id);
        if (exam == null) return;
        examId.setText(String.valueOf(exam.getExamId()));
        examName.setText(exam.getExamName());
        examSubject.setSelectedItem(subjects.searchSubject(exam.getSubjectId()));
        examDate.setText(exam.getExamDate());
        examTime.setText(exam.getExamTime());
        examType.setSelectedItem(exam.getExamType());
        Subject subject = subjects.searchSubject(exam.getSubjectId());
        int maxMarks = exam.getMaxMarks() > 0 ? exam.getMaxMarks()
            : subject == null ? 100 : subject.getMaxMarks();
        examMaxMarks.setText(String.valueOf(maxMarks));
        examStatus.setSelectedItem(exam.getExamStatus());
    }

    private void clearStudentForm() {
        studentId.setText(""); studentName.setText(""); studentEmail.setText("");
        studentCourse.setText(""); studentContact.setText("");
        studentSemester.setSelectedIndex(0);
        if (studentTable != null) studentTable.clearSelection();
    }

    private void clearSubjectForm() {
        subjectCode.setText(""); subjectName.setText(""); subjectMaxMarks.setText("");
        subjectCredits.setSelectedIndex(0); subjectSemester.setSelectedIndex(0);
        if (subjectTable != null) subjectTable.clearSelection();
    }

    private void clearExamForm() {
        examId.setText(""); examName.setText(""); examDate.setText("");
        examTime.setText(""); examMaxMarks.setText("");
        examType.setSelectedIndex(0); examStatus.setSelectedIndex(0);
        examSubject.setSelectedIndex(examSubject.getItemCount() > 0 ? 0 : -1);
        if (examTable != null) examTable.clearSelection();
    }

    private void runAction(Runnable action) {
        try {
            action.run();
            footerStatus.setText("Last action completed at " + LocalTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm")));
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check the entered information",
                JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            footerStatus.setText("Could not save changes");
            JOptionPane.showMessageDialog(this, "The operation could not be completed:\n" + ex.getMessage(),
                "Application error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirm action",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void notice(String message) {
        footerStatus.setText(message);
    }

    private void showNotice() {
        int upcoming = upcomingExams().size();
        JOptionPane.showMessageDialog(this,
            upcoming == 0 ? "There are no upcoming scheduled examinations."
                : "There are " + upcoming + " upcoming scheduled examination(s).",
            "Academic notifications", JOptionPane.INFORMATION_MESSAGE);
    }

    private String required(JTextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) throw new IllegalArgumentException(label + " is required.");
        return value;
    }

    private int integer(JTextField field, String label) {
        try {
            return Integer.parseInt(required(field, label));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    private int positiveInteger(JTextField field, String label) {
        int value = integer(field, label);
        if (value <= 0) throw new IllegalArgumentException(label + " must be greater than zero.");
        return value;
    }

    private double decimal(JTextField field, String label) {
        try {
            double value = Double.parseDouble(required(field, label));
            if (!Double.isFinite(value)) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    private <T> T selected(JComboBox<T> combo, String label) {
        int index = combo.getSelectedIndex();
        if (index < 0) throw new IllegalArgumentException("Please select " + label.toLowerCase() + ".");
        return combo.getItemAt(index);
    }

    private <T> void setComboItems(JComboBox<T> combo, List<T> values) {
        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        for (T value : values) model.addElement(value);
        combo.setModel(model);
    }

    private <T> void setRenderer(JComboBox<T> combo, ModelRenderer<T> renderer) {
        combo.setRenderer(renderer);
    }

    private void showPageForQuickAction(String page) {
        showPage(page);
        if ("Students".equals(page)) studentName.requestFocusInWindow();
        if ("Subjects".equals(page)) subjectName.requestFocusInWindow();
        if ("Examinations".equals(page)) examName.requestFocusInWindow();
        if ("Marks & Results".equals(page)) resultMarks.requestFocusInWindow();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void updateResultPreview() {
        Subject subject = (Subject) resultSubject.getSelectedItem();
        Exam exam = (Exam) resultExam.getSelectedItem();
        String entered = resultMarks.getText().trim();
        if (subject == null || entered.isEmpty()) {
            resultPreview.setText("Select a student and subject, then enter marks.");
            resultPreview.setForeground(BLUE);
            return;
        }
        if (exam != null && !exam.getSubjectId().isEmpty()
                && !exam.getSubjectId().equalsIgnoreCase(subject.getSubjectId())) {
            resultPreview.setText("Selected exam belongs to a different subject.");
            resultPreview.setForeground(new Color(220, 38, 38));
            return;
        }
        int maxMarks = exam != null && exam.getMaxMarks() > 0
            ? exam.getMaxMarks() : subject.getMaxMarks();
        try {
            double marks = Double.parseDouble(entered);
            if (!Double.isFinite(marks) || marks < 0 || marks > maxMarks) {
                throw new NumberFormatException();
            }
            double percentage = marks * 100.0 / maxMarks;
            boolean passing = percentage >= 40;
            resultPreview.setText(String.format("%.1f%%   |   %s   |   %s",
                percentage, gradeFor(percentage), passing ? "PASS" : "FAIL"));
            resultPreview.setForeground(passing ? GREEN : new Color(220, 38, 38));
        } catch (NumberFormatException ex) {
            resultPreview.setText("Marks must be between 0 and " + maxMarks);
            resultPreview.setForeground(new Color(220, 38, 38));
        }
    }

    private String gradeFor(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 40) return "D";
        return "F";
    }

    private static final class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Color fill;
        private final int radius;

        RoundedPanel(java.awt.LayoutManager layout, Color fill, int radius) {
            super(layout);
            this.fill = fill;
            this.radius = radius;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(15, 23, 42, 13));
            g.fill(new RoundRectangle2D.Float(1, 3, getWidth() - 3, getHeight() - 4, radius, radius));
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 5, radius, radius));
            g.setColor(new Color(226, 232, 240, 150));
            g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 4, getHeight() - 6, radius, radius));
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class HintTextField extends JTextField {
        private static final long serialVersionUID = 1L;
        private final String hint;

        HintTextField(String hint) {
            this.hint = hint;
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getDocument().getLength() == 0 && !hasFocus()) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(MUTED);
                g.setFont(getFont());
                g.drawString(hint, getInsets().left + 1,
                    (getHeight() + g.getFontMetrics().getAscent()
                        - g.getFontMetrics().getDescent()) / 2);
                g.dispose();
            }
        }
    }

    private static final class ZebraRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            cell.setBackground(selected ? new Color(219, 234, 254)
                : row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            cell.setForeground(selected ? new Color(30, 64, 175) : TEXT);
            if (cell instanceof JLabel) ((JLabel) cell).setBorder(
                BorderFactory.createEmptyBorder(0, 9, 0, 9));
            return cell;
        }
    }

    private static final class StatusRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final JLabel badge = new JLabel("", SwingConstants.CENTER);

        StatusRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 0, 4));
            setOpaque(true);
            badge.setOpaque(true);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            add(badge);
        }

        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int column) {
            String text = value == null ? "" : value.toString();
            badge.setText(text);
            setBackground(selected ? new Color(219, 234, 254)
                : row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            if ("PASS".equalsIgnoreCase(text) || "Scheduled".equalsIgnoreCase(text)
                    || "Completed".equalsIgnoreCase(text) || "Published".equalsIgnoreCase(text)) {
                badge.setForeground(new Color(21, 128, 61));
                badge.setBackground(new Color(220, 252, 231));
            } else if ("FAIL".equalsIgnoreCase(text) || "Cancelled".equalsIgnoreCase(text)) {
                badge.setForeground(new Color(185, 28, 28));
                badge.setBackground(new Color(254, 226, 226));
            } else if ("Draft".equalsIgnoreCase(text)) {
                badge.setForeground(new Color(71, 85, 105));
                badge.setBackground(new Color(241, 245, 249));
            } else {
                badge.setForeground(new Color(180, 83, 9));
                badge.setBackground(new Color(254, 243, 199));
            }
            return this;
        }
    }

    private abstract static class ModelRenderer<T> extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        protected abstract String text(T value);

        @SuppressWarnings("unchecked")
        @Override public Component getListCellRendererComponent(javax.swing.JList<?> list,
                Object value, int index, boolean selected, boolean focus) {
            super.getListCellRendererComponent(list, value, index, selected, focus);
            setText(text((T) value));
            setFont(BODY);
            setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            return this;
        }
    }

    private enum IconType { DASHBOARD, STUDENTS, SUBJECTS, EXAMS, RESULTS, SETTINGS, BELL }

    private static final class AppIcon implements javax.swing.Icon {
        private final IconType type;
        private final int size;
        private final Color color;

        AppIcon(IconType type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.scale(size / 24.0, size / 24.0);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND,
                java.awt.BasicStroke.JOIN_ROUND));
            switch (type) {
                case DASHBOARD:
                    g.drawRoundRect(3, 3, 8, 8, 2, 2); g.drawRoundRect(13, 3, 8, 8, 2, 2);
                    g.drawRoundRect(3, 13, 8, 8, 2, 2); g.drawRoundRect(13, 13, 8, 8, 2, 2);
                    break;
                case STUDENTS:
                    g.drawOval(8, 3, 8, 8); g.drawArc(3, 12, 18, 12, 0, 180);
                    g.drawArc(1, 9, 9, 10, 70, 120); g.drawArc(14, 9, 9, 10, -10, 120);
                    break;
                case SUBJECTS:
                    g.drawRoundRect(3, 3, 18, 18, 2, 2); g.drawLine(12, 3, 12, 21);
                    g.drawLine(6, 8, 10, 8); g.drawLine(14, 8, 18, 8);
                    g.drawLine(6, 12, 10, 12); g.drawLine(14, 12, 18, 12);
                    break;
                case EXAMS:
                    g.drawRoundRect(3, 5, 18, 16, 3, 3); g.drawLine(3, 10, 21, 10);
                    g.drawLine(8, 3, 8, 7); g.drawLine(16, 3, 16, 7);
                    g.drawLine(7, 14, 10, 17); g.drawLine(10, 17, 17, 12);
                    break;
                case RESULTS:
                    g.drawLine(4, 20, 4, 13); g.drawLine(10, 20, 10, 8);
                    g.drawLine(16, 20, 16, 4); g.drawLine(22, 20, 22, 10);
                    g.drawLine(2, 21, 23, 21);
                    break;
                case SETTINGS:
                    g.drawOval(7, 7, 10, 10); g.drawOval(10, 10, 4, 4);
                    g.drawLine(12, 2, 12, 6); g.drawLine(12, 18, 12, 22);
                    g.drawLine(2, 12, 6, 12); g.drawLine(18, 12, 22, 12);
                    break;
                case BELL:
                    g.drawArc(5, 3, 14, 17, 0, 180); g.drawLine(5, 12, 5, 18);
                    g.drawLine(19, 12, 19, 18); g.drawLine(5, 18, 19, 18);
                    g.drawArc(5, 14, 14, 10, 180, 180); g.drawOval(10, 20, 4, 3);
                    break;
                default:
                    break;
            }
            g.dispose();
        }
    }
}
