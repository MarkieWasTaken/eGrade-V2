import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AdminForm extends JFrame {

    private JTextField parentFirstName, parentLastName, parentEmail, parentPassword, parentPhone;
    private JComboBox<String> studentDropdownForParent;
    private Map<String, Integer> studentMap = new HashMap<>();

    private JTextField studentFirstName, studentLastName, studentEmail, studentPassword, studentPhone;
    private JComboBox<String> gradeDropdown;
    private Map<String, Integer> gradeMap = new HashMap<>();

    public AdminForm(int adminId) {
        setTitle("eGrade - Admin Panel");
        setSize(850, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2, 20, 0));

        JPanel parentPanel = createParentPanel();
        JPanel studentPanel = createStudentPanel();

        add(parentPanel);
        add(studentPanel);

        loadStudents();
        loadGradeLevels();

        setVisible(true);
    }

    private JPanel createParentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Create Parent & Link to Student"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        parentFirstName = new JTextField(15);
        panel.add(parentFirstName, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        parentLastName = new JTextField(15);
        panel.add(parentLastName, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        parentEmail = new JTextField(15);
        panel.add(parentEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        parentPassword = new JTextField(15);
        panel.add(parentPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        parentPhone = new JTextField(15);
        panel.add(parentPhone, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Link to Student:"), gbc);
        gbc.gridx = 1;
        studentDropdownForParent = new JComboBox<>();
        panel.add(studentDropdownForParent, gbc);

        gbc.gridx = 1;
        gbc.gridy++;
        JButton createBtn = new JButton("Create Parent");
        panel.add(createBtn, gbc);

        createBtn.addActionListener(this::createParent);

        return panel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Create Student"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        studentFirstName = new JTextField(15);
        panel.add(studentFirstName, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        studentLastName = new JTextField(15);
        panel.add(studentLastName, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        studentEmail = new JTextField(15);
        panel.add(studentEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        studentPassword = new JTextField(15);
        panel.add(studentPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        studentPhone = new JTextField(15);
        panel.add(studentPhone, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Year (Grade Level):"), gbc);
        gbc.gridx = 1;
        gradeDropdown = new JComboBox<>();
        panel.add(gradeDropdown, gbc);

        gbc.gridx = 1;
        gbc.gridy++;
        JButton createBtn = new JButton("Create Student");
        panel.add(createBtn, gbc);

        createBtn.addActionListener(this::createStudent);

        return panel;
    }

    private void createParent(ActionEvent e) {
        String fn = parentFirstName.getText().trim();
        String ln = parentLastName.getText().trim();
        String email = parentEmail.getText().trim();
        String password = parentPassword.getText().trim();
        String phone = parentPhone.getText().trim();
        String selectedStudent = (String) studentDropdownForParent.getSelectedItem();

        if (fn.isEmpty() || ln.isEmpty() || email.isEmpty() || password.isEmpty() || selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields and select a student.");
            return;
        }

        try {
            Connection conn = DatabaseManager.getConnection();

            PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO "user" (first_name, last_name, email, password, phone, user_type_id, city_id)
                VALUES (?, ?, ?, ?, ?, 1, 1)
                RETURNING id
            """);
            stmt.setString(1, fn);
            stmt.setString(2, ln);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setString(5, phone);

            ResultSet rs = stmt.executeQuery();
            int parentId = -1;
            if (rs.next()) {
                parentId = rs.getInt("id");
            }

            int studentId = studentMap.get(selectedStudent);
            PreparedStatement linkStmt = conn.prepareStatement("INSERT INTO parent_student (parent_id, student_id) VALUES (?, ?)");
            linkStmt.setInt(1, parentId);
            linkStmt.setInt(2, studentId);
            linkStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Parent created and linked to student!");
            loadStudents();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create parent.");
        }
    }

    private void createStudent(ActionEvent e) {
        String fn = studentFirstName.getText().trim();
        String ln = studentLastName.getText().trim();
        String email = studentEmail.getText().trim();
        String password = studentPassword.getText().trim();
        String phone = studentPhone.getText().trim();
        String gradeLabel = (String) gradeDropdown.getSelectedItem();

        if (fn.isEmpty() || ln.isEmpty() || email.isEmpty() || password.isEmpty() || gradeLabel == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields and select a year.");
            return;
        }

        int gradeLevelId = gradeMap.get(gradeLabel);

        try {
            Connection conn = DatabaseManager.getConnection();

            PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO "user" (first_name, last_name, email, password, phone, user_type_id, city_id)
                VALUES (?, ?, ?, ?, ?, 2, 1)
                RETURNING id
            """);
            stmt.setString(1, fn);
            stmt.setString(2, ln);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setString(5, phone);
            ResultSet rs = stmt.executeQuery();

            int studentId = -1;
            if (rs.next()) {
                studentId = rs.getInt("id");
            }

            PreparedStatement classStmt = conn.prepareStatement(
                    "SELECT id FROM class WHERE grade_level_id = ? LIMIT 1"
            );
            classStmt.setInt(1, gradeLevelId);
            ResultSet crs = classStmt.executeQuery();
            if (crs.next()) {
                int classId = crs.getInt("id");
                PreparedStatement assignClass = conn.prepareStatement(
                        "INSERT INTO student_class (student_id, class_id) VALUES (?, ?)"
                );
                assignClass.setInt(1, studentId);
                assignClass.setInt(2, classId);
                assignClass.executeUpdate();
            }

            PreparedStatement subjStmt = conn.prepareStatement("""
                INSERT INTO student_subject (student_id, subject_id)
                SELECT ?, subject_id FROM grade_level_subject WHERE grade_level_id = ?
            """);
            subjStmt.setInt(1, studentId);
            subjStmt.setInt(2, gradeLevelId);
            subjStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student created and enrolled.");
            loadStudents();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create student.");
        }
    }

    private void loadStudents() {
        studentDropdownForParent.removeAllItems();
        studentMap.clear();

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT id, first_name || ' ' || last_name AS full_name FROM \"user\" WHERE user_type_id = 2"
        )) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("full_name");
                studentDropdownForParent.addItem(name);
                studentMap.put(name, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGradeLevels() {
        gradeDropdown.removeAllItems();
        gradeMap.clear();

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT id, year FROM grade_level ORDER BY id"
        )) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String label = "Year " + rs.getString("year");
                gradeDropdown.addItem(label);
                gradeMap.put(label, rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
