import java.awt.*;
import javax.swing.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class ParentForm extends JFrame {

    public ParentForm(int parentId) {
        setTitle("eGrade – Parent Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(245, 248, 255));

        Map<String, Map<String, List<GradeItem>>> studentSubjectGrades = fetchAllStudentGrades(parentId);

        if (studentSubjectGrades.isEmpty()) {
            JLabel emptyLabel = new JLabel("No students found for this parent.");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(emptyLabel);
        }

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int topPadding = (screen.height / 2) - 200;
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));


        for (String studentName : studentSubjectGrades.keySet()) {
            JLabel title = new JLabel(studentName + "'s Grades");
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));
            mainPanel.add(title);

            Map<String, List<GradeItem>> subjectGrades = studentSubjectGrades.get(studentName);

            for (Map.Entry<String, List<GradeItem>> entry : subjectGrades.entrySet()) {
                String subject = entry.getKey();
                List<GradeItem> grades = entry.getValue();
                double average = grades.stream().mapToDouble(g -> g.score).average().orElse(0.0);
                String teacherName = fetchTeacherForSubject(subject);

                mainPanel.add(createSubjectCard(subject, teacherName, grades, average));
                mainPanel.add(Box.createVerticalStrut(10));
            }
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);
        setVisible(true);

        JMenuBar menuBar = new JMenuBar();
        JMenu accountMenu = new JMenu("Profile");
        JMenuItem logoutItem = new JMenuItem("Log Out");

        logoutItem.addActionListener(e -> {
            dispose();
            new LoginForm();
        });

        accountMenu.add(logoutItem);
        menuBar.add(accountMenu);
        setJMenuBar(menuBar);
    }

    static class GradeItem {
        int score;
        String comment;

        public GradeItem(int score, String comment) {
            this.score = score;
            this.comment = comment;
        }
    }

    private Map<String, Map<String, List<GradeItem>>> fetchAllStudentGrades(int parentId) {
        Map<String, Map<String, List<GradeItem>>> studentData = new LinkedHashMap<>();

        String query = """
            SELECT u.id as student_id, u.first_name || ' ' || u.last_name AS student_name,
                   s.name AS subject, g.score, g.comment
            FROM parent_student ps
            JOIN "user" u ON u.id = ps.student_id
            JOIN student_class sc ON sc.student_id = u.id
            JOIN class c ON c.id = sc.class_id
            JOIN grade_level_subject gls ON gls.grade_level_id = c.grade_level_id
            JOIN subject s ON s.id = gls.subject_id
            LEFT JOIN grade g ON g.student_id = u.id AND g.subject_id = s.id
            WHERE ps.parent_id = ?
            ORDER BY student_name, s.name
        """;

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(query)) {
            stmt.setInt(1, parentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String studentName = rs.getString("student_name");
                String subject = rs.getString("subject");
                int score = rs.getInt("score");
                String comment = rs.getString("comment");

                studentData.computeIfAbsent(studentName, k -> new LinkedHashMap<>());
                Map<String, List<GradeItem>> subjects = studentData.get(studentName);
                subjects.computeIfAbsent(subject, k -> new ArrayList<>());

                if (score != 0) {
                    subjects.get(subject).add(new GradeItem(score, comment));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return studentData;
    }

    private String fetchTeacherForSubject(String subjectName) {
        String query = """
            SELECT u.first_name || ' ' || u.last_name AS full_name
            FROM teacher_subject ts
            JOIN subject s ON s.id = ts.subject_id
            JOIN "user" u ON u.id = ts.teacher_id
            WHERE s.name ILIKE ? LIMIT 1
        """;

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(query)) {
            stmt.setString(1, subjectName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("full_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private JPanel createSubjectCard(String subjectName, String teacherName, List<GradeItem> grades, double avg) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        Color subjectColor = getSubjectColor(subjectName);

        JLabel subjectLabel = new JLabel(subjectName.toUpperCase());
        subjectLabel.setOpaque(true);
        subjectLabel.setForeground(Color.WHITE);
        subjectLabel.setBackground(subjectColor);
        subjectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subjectLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JLabel teacherLabel = new JLabel(teacherName);
        teacherLabel.setOpaque(true);
        teacherLabel.setForeground(Color.WHITE);
        teacherLabel.setBackground(subjectColor.darker());
        teacherLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        teacherLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topPanel.setOpaque(false);
        topPanel.add(subjectLabel);
        topPanel.add(teacherLabel);
        card.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel gradePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        gradePanel.setOpaque(false);

        if (grades.isEmpty()) {
            JLabel noGrade = new JLabel("No grades yet");
            noGrade.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noGrade.setForeground(Color.GRAY);
            gradePanel.add(noGrade);
        }

        for (GradeItem grade : grades) {
            JLabel badge = new JLabel(String.valueOf(grade.score), SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(32, 32));
            badge.setOpaque(true);
            badge.setForeground(Color.WHITE);
            badge.setBackground(getGradeColor(grade.score));
            badge.setFont(new Font("Segoe UI", Font.BOLD, 14));
            badge.setToolTipText(grade.comment != null ? grade.comment : "No comment");
            badge.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            gradePanel.add(badge);
        }

        centerPanel.add(gradePanel, BorderLayout.WEST);

        JLabel avgLabel = new JLabel(grades.isEmpty() ? "—" : String.format("%.2f", avg));
        avgLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        avgLabel.setForeground(new Color(44, 62, 80));
        avgLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel avgWrapper = new JPanel(new BorderLayout());
        avgWrapper.setOpaque(false);
        avgWrapper.add(avgLabel, BorderLayout.EAST);
        avgWrapper.setPreferredSize(new Dimension(80, 40));
        centerPanel.add(avgWrapper, BorderLayout.EAST);

        card.add(centerPanel, BorderLayout.CENTER);
        return card;
    }

    private Color getSubjectColor(String subject) {
        return switch (subject.toLowerCase()) {
            case "mathematics", "math", "math+" -> new Color(0, 123, 255);
            case "science" -> new Color(26, 188, 156);
            case "history" -> new Color(241, 196, 15);
            case "english", "english+" -> new Color(231, 76, 60);
            case "programming", "programming+" -> new Color(52, 152, 219);
            case "ict" -> new Color(93, 173, 226);
            case "art" -> new Color(142, 68, 173);
            case "music" -> new Color(243, 156, 18);
            case "biology" -> new Color(39, 174, 96);
            case "sports" -> new Color(230, 126, 34);
            case "german" -> new Color(192, 57, 43);
            default -> new Color(120, 144, 156);
        };
    }

    private Color getGradeColor(int score) {
        if (score >= 90) return new Color(46, 204, 113);
        else if (score > 75) return new Color(129, 199, 132);
        else if (score > 60) return new Color(255, 152, 0);
        else if (score > 50) return new Color(255, 235, 59);
        else return new Color(231, 76, 60);
    }
}
