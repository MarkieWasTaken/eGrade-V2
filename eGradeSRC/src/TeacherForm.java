import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class TeacherForm extends JFrame {

    private JComboBox<String> subjectComboBox;
    private JTable studentTable;
    private JComboBox<String> studentDropdown;
    private JTextField scoreField;
    private JTextField dateField;
    private JTextField commentField;
    private JButton submitGradeButton;

    private int teacherId;
    private Map<String, Integer> subjectMap = new HashMap<>();
    private Map<String, Integer> studentMap = new HashMap<>();

    public TeacherForm(int teacherId) {
        this.teacherId = teacherId;
        setTitle("eGrade - Teacher Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 248, 255));
        add(mainPanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel welcomeLabel = new JLabel("👨‍🏫 Welcome, Teacher (ID: " + teacherId + ")");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        JButton logoutButton = new JButton("Logout");
        topPanel.add(logoutButton, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel subjectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        subjectPanel.setOpaque(false);
        subjectPanel.add(new JLabel("Subject:"));
        subjectComboBox = new JComboBox<>();
        subjectPanel.add(subjectComboBox);
        mainPanel.add(subjectPanel, BorderLayout.BEFORE_FIRST_LINE);

        studentTable = new JTable(new DefaultTableModel(
                new Object[]{"Student Name", "Average Grade", "# of Grades"}, 0
        ));
        JScrollPane scrollPane = new JScrollPane(studentTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel addGradePanel = new JPanel(new GridBagLayout());
        addGradePanel.setBackground(new Color(235, 245, 255));
        addGradePanel.setBorder(BorderFactory.createTitledBorder("Add Grade"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        addGradePanel.add(new JLabel("Student:"), gbc);

        gbc.gridx = 1;
        studentDropdown = new JComboBox<>();
        addGradePanel.add(studentDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        addGradePanel.add(new JLabel("Score:"), gbc);

        gbc.gridx = 1;
        scoreField = new JTextField(5);
        addGradePanel.add(scoreField, gbc);

        gbc.gridx = 2;
        addGradePanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 3;
        dateField = new JTextField(10);
        addGradePanel.add(dateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        addGradePanel.add(new JLabel("Comment:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        commentField = new JTextField(30);
        addGradePanel.add(commentField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        submitGradeButton = new JButton("Submit Grade");
        addGradePanel.add(submitGradeButton, gbc);

        mainPanel.add(addGradePanel, BorderLayout.SOUTH);

        subjectComboBox.addActionListener(e -> loadStudentsAndGrades());
        submitGradeButton.addActionListener(e -> insertGrade());

        loadSubjects();
        setVisible(true);

        JMenuBar menuBar = new JMenuBar();
        JMenu accountMenu = new JMenu("Profile");
        JMenuItem logoutItem = new JMenuItem("Log Out");

        logoutItem.addActionListener(e -> {
            dispose();
            new LoginForm(); // Return to login
        });

        accountMenu.add(logoutItem);
        menuBar.add(accountMenu);
        setJMenuBar(menuBar);

    }

    private void loadSubjects() {
        subjectMap.clear();
        subjectComboBox.removeAllItems();

        String sql = "SELECT * FROM get_teacher_subjects(?)";

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                subjectMap.put(name, id);
                subjectComboBox.addItem(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (subjectComboBox.getItemCount() > 0) {
            subjectComboBox.setSelectedIndex(0);
            loadStudentsAndGrades();
        }
    }

    private void loadStudentsAndGrades() {
        studentMap.clear();
        studentDropdown.removeAllItems();
        DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
        model.setRowCount(0);

        String selectedSubject = (String) subjectComboBox.getSelectedItem();
        if (selectedSubject == null) return;
        int subjectId = subjectMap.get(selectedSubject);

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT * FROM get_students_with_grades(?)"
        )) {
            stmt.setInt(1, subjectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int studentId = rs.getInt("student_id"); // ✅ match with function output
                String name = rs.getString("full_name");
                double avg = rs.getDouble("avg_grade");
                int count = rs.getInt("grade_count");

                model.addRow(new Object[]{name, avg, count});
                studentMap.put(name, studentId);
                studentDropdown.addItem(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertGrade() {
        String studentName = (String) studentDropdown.getSelectedItem();
        String subjectName = (String) subjectComboBox.getSelectedItem();
        String scoreText = scoreField.getText().trim();
        String date = dateField.getText().trim();
        String comment = commentField.getText().trim();

        if (studentName == null || subjectName == null || scoreText.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.");
            return;
        }

        int studentId = studentMap.get(studentName);
        int subjectId = subjectMap.get(subjectName);
        int score;

        try {
            score = Integer.parseInt(scoreText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid score. Must be a number.");
            return;
        }

        String sql = "SELECT * FROM add_grade(?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            stmt.setInt(2, studentId);
            stmt.setInt(3, subjectId);
            stmt.setInt(4, score);
            stmt.setDate(5, Date.valueOf(date));
            stmt.setString(6, comment);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean success = rs.getBoolean("success");
                String message = rs.getString("message");

                JOptionPane.showMessageDialog(this, message);
                if (success) {
                    scoreField.setText("");
                    commentField.setText("");
                    dateField.setText("");
                    loadStudentsAndGrades();
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error occurred.");
            e.printStackTrace();
        }
    }








}
