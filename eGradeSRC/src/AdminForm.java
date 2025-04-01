import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import org.mindrot.jbcrypt.BCrypt;


public class AdminForm extends JFrame {

    private final Map<String, Integer> studentMap = new HashMap<>();
    private final Map<String, Integer> cityMap = new HashMap<>();

    private JComboBox<String> linkStudentDropdown, parentCityDropdown, studentCityDropdown;
    private JTextField parentFirstName, parentLastName, parentEmail, parentPassword, parentPhone;
    private JTextField studentFirstName, studentLastName, studentEmail, studentPhone;
    private JComboBox<String> studentYearCombo;

    public AdminForm(int adminId) {
        setTitle("eGrade - Admin Panel");
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Create Student", createStudentPanel());
        tabbedPane.addTab("Create Parent", createParentPanel());
        tabbedPane.addTab("Manage Users", createUserManagementPanel());
        tabbedPane.addTab("Grade History", createGradeLogPanel());



        add(tabbedPane);
        loadStudents();
        loadCities();
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
    private JPanel createGradeLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnNames = {
                "Teacher", "Student", "Subject",
                "Score", "Comment", "Date Given", "Timestamp"
        };

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("""
        SELECT 
            t.first_name || ' ' || t.last_name AS teacher,
            s.first_name || ' ' || s.last_name AS student,
            subj.name AS subject,
            gl.score,
            gl.comment,
            gl.date_given,
            gl.timestamp
        FROM grade_log gl
        JOIN "user" t ON gl.teacher_id = t.id
        JOIN "user" s ON gl.student_id = s.id
        JOIN subject subj ON gl.subject_id = subj.id
        ORDER BY gl.timestamp DESC
    """)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("teacher"),
                        rs.getString("student"),
                        rs.getString("subject"),
                        rs.getDouble("score"),
                        rs.getString("comment"),
                        rs.getDate("date_given"),
                        rs.getTimestamp("timestamp")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return panel;
    }


    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === Table setup ===
        String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Phone", "User Type", "City"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // === Edit Form Panel ===
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Edit Selected User"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField idField = new JTextField(5);
        JTextField fnField = new JTextField(10);
        JTextField lnField = new JTextField(10);
        JTextField emailField = new JTextField(15);
        JTextField phoneField = new JTextField(10);
        JComboBox<String> typeDropdown = new JComboBox<>();
        JComboBox<String> cityDropdown = new JComboBox<>();

        Map<String, Integer> typeMap = new HashMap<>();
        Map<String, Integer> cityMapLocal = new HashMap<>();

        // Fill dropdowns from DB
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT id, type_name FROM user_type")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String type = rs.getString("type_name");
                typeDropdown.addItem(type);
                typeMap.put(type, rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT id, name FROM city ORDER BY name")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String city = rs.getString("name");
                cityDropdown.addItem(city);
                cityMapLocal.put(city, rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // === Grid Form Layout ===
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; formPanel.add(idField, gbc); idField.setEditable(false);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1; formPanel.add(fnField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1; formPanel.add(lnField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; formPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; formPanel.add(typeDropdown, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("City:"), gbc);
        gbc.gridx = 1; formPanel.add(cityDropdown, gbc);

        // === Buttons ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton updateBtn = new JButton("Update User");
        JButton deleteBtn = new JButton("Delete User");
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        panel.add(formPanel, BorderLayout.SOUTH);

        // === Table Selection Listener ===
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                idField.setText(table.getValueAt(row, 0).toString());
                fnField.setText(table.getValueAt(row, 1).toString());
                lnField.setText(table.getValueAt(row, 2).toString());
                emailField.setText(table.getValueAt(row, 3).toString());
                phoneField.setText(table.getValueAt(row, 4).toString());
                typeDropdown.setSelectedItem(table.getValueAt(row, 5).toString());
                cityDropdown.setSelectedItem(table.getValueAt(row, 6).toString());
            }
        });

        // === Button Actions ===
        updateBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                String fn = fnField.getText().trim();
                String ln = lnField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                int typeId = typeMap.get((String) typeDropdown.getSelectedItem());
                int cityId = cityMapLocal.get((String) cityDropdown.getSelectedItem());

                PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("""
                UPDATE "user" SET first_name = ?, last_name = ?, email = ?, phone = ?, user_type_id = ?, city_id = ? WHERE id = ?
            """);
                stmt.setString(1, fn);
                stmt.setString(2, ln);
                stmt.setString(3, email);
                stmt.setString(4, phone);
                stmt.setInt(5, typeId);
                stmt.setInt(6, cityId);
                stmt.setInt(7, id);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(panel, "User updated!");
                refreshUserTable(tableModel);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Failed to update user.");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            int id = Integer.parseInt(idField.getText());
            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("DELETE FROM \"user\" WHERE id = ?");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(panel, "User deleted.");
                    refreshUserTable(tableModel);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(panel, "Failed to delete user.");
                }
            }
        });

        // === Initial Load ===
        refreshUserTable(tableModel);

        return panel;
    }

    private void refreshUserTable(DefaultTableModel model) {
        model.setRowCount(0);
        String query = """
        SELECT u.id, u.first_name, u.last_name, u.email, u.phone,
               ut.type_name, c.name AS city
        FROM "user" u
        JOIN user_type ut ON u.user_type_id = ut.id
        JOIN city c ON u.city_id = c.id
        ORDER BY u.id
    """;

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("type_name"),
                        rs.getString("city")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("New Student"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        studentFirstName = new JTextField(15); panel.add(studentFirstName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        studentLastName = new JTextField(15); panel.add(studentLastName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        studentEmail = new JTextField(15); panel.add(studentEmail, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        studentPhone = new JTextField(15); panel.add(studentPhone, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("City:"), gbc);
        gbc.gridx = 1;
        studentCityDropdown = new JComboBox<>(); panel.add(studentCityDropdown, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Year (1-9):"), gbc);
        gbc.gridx = 1;
        studentYearCombo = new JComboBox<>();
        for (int i = 1; i <= 9; i++) studentYearCombo.addItem(String.valueOf(i));
        panel.add(studentYearCombo, gbc);

        gbc.gridx = 1; gbc.gridy++;
        JButton createBtn = new JButton("Create Student");
        createBtn.addActionListener(this::createStudent);
        panel.add(createBtn, gbc);

        return panel;
    }

    private JPanel createParentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("New Parent"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        parentFirstName = new JTextField(15); panel.add(parentFirstName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        parentLastName = new JTextField(15); panel.add(parentLastName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        parentEmail = new JTextField(15); panel.add(parentEmail, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        parentPassword = new JTextField(15); panel.add(parentPassword, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        parentPhone = new JTextField(15); panel.add(parentPhone, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("City:"), gbc);
        gbc.gridx = 1;
        parentCityDropdown = new JComboBox<>(); panel.add(parentCityDropdown, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Link to Student:"), gbc);
        gbc.gridx = 1;
        linkStudentDropdown = new JComboBox<>(); panel.add(linkStudentDropdown, gbc);

        gbc.gridx = 1; gbc.gridy++;
        JButton createBtn = new JButton("Create Parent");
        createBtn.addActionListener(this::createParent);
        panel.add(createBtn, gbc);

        return panel;
    }

    private void createStudent(ActionEvent e) {
        String fn = studentFirstName.getText().trim();
        String ln = studentLastName.getText().trim();
        String email = studentEmail.getText().trim();
        String phone = studentPhone.getText().trim();
        String city = (String) studentCityDropdown.getSelectedItem();
        int cityId = cityMap.getOrDefault(city, 1);
        int year = Integer.parseInt((String) studentYearCombo.getSelectedItem());

        if (fn.isEmpty() || ln.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try {
            String rawPassword = fn.toLowerCase() + "2024";
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

            Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("""
            INSERT INTO "user" (first_name, last_name, email, password, phone, user_type_id, city_id)
            VALUES (?, ?, ?, ?, ?, 2, ?)
            RETURNING id
        """);
            stmt.setString(1, fn);
            stmt.setString(2, ln);
            stmt.setString(3, email);
            stmt.setString(4, hashedPassword);
            stmt.setString(5, phone);
            stmt.setInt(6, cityId);
            ResultSet rs = stmt.executeQuery();

            int studentId = -1;
            if (rs.next()) studentId = rs.getInt("id");

            // 👇 Insert student classes
            PreparedStatement classStmt = conn.prepareStatement("""
            INSERT INTO student_class (student_id, class_id)
            SELECT ?, id FROM class WHERE grade_level_id = ?
        """);
            classStmt.setInt(1, studentId);
            classStmt.setInt(2, year);
            classStmt.executeUpdate();

            // ✅ INSERT SUBJECTS for the student
            PreparedStatement subjectStmt = conn.prepareStatement("""
            INSERT INTO student_subject (student_id, subject_id)
            SELECT ?, subject_id FROM grade_level_subject
            WHERE grade_level_id = ?
        """);
            subjectStmt.setInt(1, studentId);
            subjectStmt.setInt(2, year);
            subjectStmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Student created successfully!\nPassword: " + rawPassword,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            loadStudents();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create student.");
        }
    }



    private void createParent(ActionEvent e) {
        String fn = parentFirstName.getText().trim();
        String ln = parentLastName.getText().trim();
        String email = parentEmail.getText().trim();
        String password = parentPassword.getText().trim();
        String phone = parentPhone.getText().trim();
        String city = (String) parentCityDropdown.getSelectedItem();
        int cityId = cityMap.getOrDefault(city, 1);
        String selectedStudent = (String) linkStudentDropdown.getSelectedItem();

        if (fn.isEmpty() || ln.isEmpty() || email.isEmpty() || password.isEmpty() || selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields and select a student.");
            return;
        }

        try {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // ✅ Hash it!

            Connection conn = DatabaseManager.getConnection();

            PreparedStatement stmt = conn.prepareStatement("""
            INSERT INTO "user" (first_name, last_name, email, password, phone, user_type_id, city_id)
            VALUES (?, ?, ?, ?, ?, 1, ?)
            RETURNING id
        """);
            stmt.setString(1, fn);
            stmt.setString(2, ln);
            stmt.setString(3, email);
            stmt.setString(4, hashedPassword); // ✅ use it here
            stmt.setString(5, phone);
            stmt.setInt(6, cityId);
            ResultSet rs = stmt.executeQuery();

            int parentId = -1;
            if (rs.next()) parentId = rs.getInt("id");

            int studentId = studentMap.get(selectedStudent);
            PreparedStatement linkStmt = conn.prepareStatement("""
            INSERT INTO parent_student (parent_id, student_id)
            VALUES (?, ?)
        """);
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


    private void loadStudents() {
        studentMap.clear();
        if (linkStudentDropdown != null) linkStudentDropdown.removeAllItems();

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("""
            SELECT id, first_name || ' ' || last_name || '|TEL| ' || phone AS full_name
            FROM "user"
            WHERE user_type_id = 2
        """)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("full_name");
                studentMap.put(name, id);
                if (linkStudentDropdown != null) linkStudentDropdown.addItem(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCities() {
        cityMap.clear();
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("""
            SELECT DISTINCT id, name FROM city ORDER BY name
        """)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                cityMap.put(name, id);
                if (parentCityDropdown != null) parentCityDropdown.addItem(name);
                if (studentCityDropdown != null) studentCityDropdown.addItem(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
