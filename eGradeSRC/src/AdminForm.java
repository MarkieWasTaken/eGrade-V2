// AdminForm.java
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AdminForm extends JFrame {

    private final Map<String, Integer> studentMap = new HashMap<>();
    private final Map<String, Integer> cityMap = new HashMap<>();

    private JComboBox<String> linkStudentDropdown, parentCityDropdown, studentCityDropdown;
    private JTextField parentFirstName, parentLastName, parentEmail, parentPassword, parentPhone;
    private JTextField studentFirstName, studentLastName, studentEmail, studentPhone;
    private JComboBox<String> studentYearCombo;

    public AdminForm(int adminId) {
        setTitle("eGrade - Admin Panel");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Create Student", createStudentPanel());
        tabs.addTab("Create Parent", createParentPanel());
        tabs.addTab("Create Teacher", createTeacherPanel());
        tabs.addTab("Manage Users", createUserManagementPanel());
        tabs.addTab("Grade History", createGradeLogPanel());

        add(tabs);
        loadStudents();
        loadCities();
        setVisible(true);

        JMenuBar menuBar = new JMenuBar();
        JMenu accountMenu = new JMenu("Profile");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> {
            dispose();
            new LoginForm();
        });
        accountMenu.add(logoutItem);
        menuBar.add(accountMenu);
        setJMenuBar(menuBar);
    }

    private JPanel createTeacherPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Create Teacher"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField fnField = new JTextField(15);
        JTextField lnField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JTextField passwordField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JComboBox<String> cityDropdown = new JComboBox<>();
        JComboBox<String> subjectDropdown = new JComboBox<>();

        Map<String, Integer> cityMapLocal = new HashMap<>();
        Map<String, Integer> subjectMap = new HashMap<>();

        // Load Cities
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT id, name FROM city")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                cityDropdown.addItem(name);
                cityMapLocal.put(name, rs.getInt("id"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Load Subjects
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT id, name FROM subject")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                subjectDropdown.addItem(name);
                subjectMap.put(name, rs.getInt("id"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("First Name:"), gbc); gbc.gridx = 1; panel.add(fnField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Last Name:"), gbc); gbc.gridx = 1; panel.add(lnField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Email:"), gbc); gbc.gridx = 1; panel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Password:"), gbc); gbc.gridx = 1; panel.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Phone:"), gbc); gbc.gridx = 1; panel.add(phoneField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("City:"), gbc); gbc.gridx = 1; panel.add(cityDropdown, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Subject:"), gbc); gbc.gridx = 1; panel.add(subjectDropdown, gbc);

        JButton createBtn = new JButton("Create Teacher");
        gbc.gridx = 1; gbc.gridy = row++;
        panel.add(createBtn, gbc);

        createBtn.addActionListener(e -> {
            try {
                String fn = fnField.getText();
                String ln = lnField.getText();
                String email = emailField.getText();
                String pass = passwordField.getText();
                String phone = phoneField.getText();
                int cityId = cityMapLocal.get(cityDropdown.getSelectedItem());
                int subjectId = subjectMap.get(subjectDropdown.getSelectedItem());

                PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT * FROM create_teacher(?, ?, ?, ?, ?, ?, ?)"
                );
                stmt.setString(1, fn);
                stmt.setString(2, ln);
                stmt.setString(3, email);
                stmt.setString(4, pass);
                stmt.setString(5, phone);
                stmt.setInt(6, cityId);
                stmt.setInt(7, subjectId);

                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getBoolean("success")) {
                    JOptionPane.showMessageDialog(panel, "✅ Teacher created! ID: " + rs.getInt("id"));
                } else {
                    JOptionPane.showMessageDialog(panel, "❌ Failed: " + rs.getString("message"));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Error creating teacher.");
            }
        });

        return panel;
    }



    private JPanel createGradeLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = { "Teacher", "Student", "Subject", "Score", "Comment", "Date Given", "Logged At" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT * FROM get_grade_log()")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("teacher"),
                        rs.getString("student"),
                        rs.getString("subject"),
                        rs.getDouble("score"),
                        rs.getString("comment"),
                        rs.getDate("date_given"),
                        rs.getTimestamp("logged_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return panel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Create Student"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        studentFirstName = new JTextField(15);
        studentLastName = new JTextField(15);
        studentEmail = new JTextField(15);
        studentPhone = new JTextField(15);
        studentCityDropdown = new JComboBox<>();
        studentYearCombo = new JComboBox<>();
        for (int i = 1; i <= 9; i++) studentYearCombo.addItem(String.valueOf(i));

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("First Name:"), gbc); gbc.gridx = 1; panel.add(studentFirstName, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Last Name:"), gbc); gbc.gridx = 1; panel.add(studentLastName, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Email:"), gbc); gbc.gridx = 1; panel.add(studentEmail, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Phone:"), gbc); gbc.gridx = 1; panel.add(studentPhone, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("City:"), gbc); gbc.gridx = 1; panel.add(studentCityDropdown, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Grade Level:"), gbc); gbc.gridx = 1; panel.add(studentYearCombo, gbc);

        JButton btn = new JButton("Create Student");
        btn.addActionListener(this::createStudent);
        gbc.gridx = 1; gbc.gridy = row++; panel.add(btn, gbc);

        return panel;
    }

    private JPanel createParentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Create Parent"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        parentFirstName = new JTextField(15);
        parentLastName = new JTextField(15);
        parentEmail = new JTextField(15);
        parentPassword = new JTextField(15);
        parentPhone = new JTextField(15);
        parentCityDropdown = new JComboBox<>();
        linkStudentDropdown = new JComboBox<>();

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("First Name:"), gbc); gbc.gridx = 1; panel.add(parentFirstName, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Last Name:"), gbc); gbc.gridx = 1; panel.add(parentLastName, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Email:"), gbc); gbc.gridx = 1; panel.add(parentEmail, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Password:"), gbc); gbc.gridx = 1; panel.add(parentPassword, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Phone:"), gbc); gbc.gridx = 1; panel.add(parentPhone, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("City:"), gbc); gbc.gridx = 1; panel.add(parentCityDropdown, gbc);
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Link to Student:"), gbc); gbc.gridx = 1; panel.add(linkStudentDropdown, gbc);

        JButton btn = new JButton("Create Parent");
        btn.addActionListener(this::createParent);
        gbc.gridx = 1; gbc.gridy = row++; panel.add(btn, gbc);

        return panel;
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"ID", "First Name", "Last Name", "Email", "Phone", "User Type", "City"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // === Edit form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Edit User"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        JTextField idField = new JTextField(5); idField.setEditable(false);
        JTextField fnField = new JTextField(10);
        JTextField lnField = new JTextField(10);
        JTextField emailField = new JTextField(15);
        JTextField phoneField = new JTextField(10);
        JComboBox<String> typeDropdown = new JComboBox<>();
        JComboBox<String> cityDropdown = new JComboBox<>();

        Map<String, Integer> typeMap = new HashMap<>();
        Map<String, Integer> cityMapLocal = new HashMap<>();

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT id, type_name FROM user_type")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                typeDropdown.addItem(rs.getString("type_name"));
                typeMap.put(rs.getString("type_name"), rs.getInt("id"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT id, name FROM city")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cityDropdown.addItem(rs.getString("name"));
                cityMapLocal.put(rs.getString("name"), rs.getInt("id"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; form.add(new JLabel("ID:"), gbc); gbc.gridx = 1; form.add(idField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; form.add(new JLabel("First Name:"), gbc); gbc.gridx = 1; form.add(fnField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; form.add(new JLabel("Last Name:"), gbc); gbc.gridx = 1; form.add(lnField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; form.add(new JLabel("Email:"), gbc); gbc.gridx = 1; form.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; form.add(new JLabel("Phone:"), gbc); gbc.gridx = 1; form.add(phoneField, gbc);
        gbc.gridx = 0; gbc.gridy = row++; form.add(new JLabel("Type:"), gbc); gbc.gridx = 1; form.add(typeDropdown, gbc);
        gbc.gridx = 0; gbc.gridy = row++; form.add(new JLabel("City:"), gbc); gbc.gridx = 1; form.add(cityDropdown, gbc);

        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");

        updateBtn.addActionListener(e -> {
            try {
                PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT * FROM update_user(?, ?, ?, ?, ?, ?, ?)");
                stmt.setInt(1, Integer.parseInt(idField.getText()));
                stmt.setString(2, fnField.getText());
                stmt.setString(3, lnField.getText());
                stmt.setString(4, emailField.getText());
                stmt.setString(5, phoneField.getText());
                stmt.setInt(6, typeMap.get(typeDropdown.getSelectedItem().toString()));
                stmt.setInt(7, cityMapLocal.get(cityDropdown.getSelectedItem().toString()));
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getBoolean("success")) {
                    refreshUserTable(model);
                    JOptionPane.showMessageDialog(panel, "User updated");
                } else {
                    JOptionPane.showMessageDialog(panel, "Failed: " + rs.getString("message"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        deleteBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT * FROM delete_user(?)");
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getBoolean("success")) {
                    refreshUserTable(model);
                    JOptionPane.showMessageDialog(panel, "User deleted.");
                } else {
                    JOptionPane.showMessageDialog(panel, "Delete failed: " + rs.getString("message"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int rowIdx = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && rowIdx != -1) {
                idField.setText(table.getValueAt(rowIdx, 0).toString());
                fnField.setText(table.getValueAt(rowIdx, 1).toString());
                lnField.setText(table.getValueAt(rowIdx, 2).toString());
                emailField.setText(table.getValueAt(rowIdx, 3).toString());
                phoneField.setText(table.getValueAt(rowIdx, 4).toString());
                typeDropdown.setSelectedItem(table.getValueAt(rowIdx, 5).toString());
                cityDropdown.setSelectedItem(table.getValueAt(rowIdx, 6).toString());
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(updateBtn); btnPanel.add(deleteBtn);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        panel.add(form, BorderLayout.SOUTH);
        refreshUserTable(model);
        return panel;
    }

    private void refreshUserTable(DefaultTableModel model) {
        model.setRowCount(0);
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT * FROM get_all_users()")) {
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


    private void createStudent(ActionEvent e) {
        try {
            String fn = studentFirstName.getText();
            String ln = studentLastName.getText();
            String email = studentEmail.getText();
            String phone = studentPhone.getText();
            String password = fn.toLowerCase() + "2024";
            int cityId = cityMap.get(studentCityDropdown.getSelectedItem());
            int grade = Integer.parseInt((String) studentYearCombo.getSelectedItem());

            PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT * FROM create_student(?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, fn);
            stmt.setString(2, ln);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setString(5, phone);
            stmt.setInt(6, cityId);
            stmt.setInt(7, grade);

            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getBoolean("success")) {
                JOptionPane.showMessageDialog(this, "Student created! Password: " + password);
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + rs.getString("message"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create student.");
        }
    }

    private void createParent(ActionEvent e) {
        try {
            String fn = parentFirstName.getText();
            String ln = parentLastName.getText();
            String email = parentEmail.getText();
            String pass = parentPassword.getText();
            String phone = parentPhone.getText();
            int cityId = cityMap.get(parentCityDropdown.getSelectedItem());
            int studentId = studentMap.get(linkStudentDropdown.getSelectedItem());

            PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT * FROM create_parent(?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, fn);
            stmt.setString(2, ln);
            stmt.setString(3, email);
            stmt.setString(4, pass);
            stmt.setString(5, phone);
            stmt.setInt(6, cityId);
            stmt.setInt(7, studentId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getBoolean("success")) {
                JOptionPane.showMessageDialog(this, "Parent created.");
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, "Failed: " + rs.getString("message"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating parent.");
        }
    }

    private void loadStudents() {
        studentMap.clear();
        if (linkStudentDropdown != null) linkStudentDropdown.removeAllItems();

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("""
            SELECT id, first_name || ' ' || last_name AS full_name FROM "user" WHERE user_type_id = 2
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
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement("SELECT id, name FROM city ORDER BY name")) {
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
