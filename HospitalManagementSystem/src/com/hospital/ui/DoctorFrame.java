package com.hospital.ui;

import com.hospital.dao.DoctorDAO;
import com.hospital.model.Doctor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;


public class DoctorFrame extends JFrame {

    private final DoctorDAO doctorDAO = new DoctorDAO();

    private JTextField idField, nameField, phoneField, emailField, experienceField;
    private JComboBox<String> specializationCombo;
    private JComboBox<String> availableCombo;
    private JTable table;
    private DefaultTableModel tableModel;

    private static final String[] SPECIALIZATIONS = {
            "Cardiology", "Neurology", "Orthopedics", "Pediatrics",
            "General Medicine", "Dermatology", "ENT", "Gynecology"
    };

    public DoctorFrame() {
        setTitle("Doctor Management - CityCare HMS");
        setSize(1000, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);

        root.add(UITheme.createHeader("Doctor Management"), BorderLayout.NORTH);
        root.add(buildFormPanel(), BorderLayout.WEST);
        root.add(buildTablePanel(), BorderLayout.CENTER);

        loadAllDoctors();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(320, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridx = 0;
        int row = 0;

        idField = UITheme.createTextField();
        idField.setEditable(false);
        idField.setBackground(new Color(0xF0F0F0));

        nameField = UITheme.createTextField();
        specializationCombo = new JComboBox<>(SPECIALIZATIONS);
        specializationCombo.setFont(UITheme.FONT_FIELD);
        specializationCombo.setEditable(true);
        phoneField = UITheme.createTextField();
        emailField = UITheme.createTextField();
        experienceField = UITheme.createTextField();
        availableCombo = new JComboBox<>(new String[]{"YES", "NO"});
        availableCombo.setFont(UITheme.FONT_FIELD);

        row = addFieldComponent(panel, gbc, row, "Doctor ID", idField);
        row = addFieldComponent(panel, gbc, row, "Name *", nameField);
        row = addFieldComponent(panel, gbc, row, "Specialization *", specializationCombo);
        row = addFieldComponent(panel, gbc, row, "Phone *", phoneField);
        row = addFieldComponent(panel, gbc, row, "Email", emailField);
        row = addFieldComponent(panel, gbc, row, "Experience (years) *", experienceField);
        row = addFieldComponent(panel, gbc, row, "Available", availableCombo);

        JPanel buttons = new JPanel(new GridLayout(3, 2, 8, 8));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton addBtn = UITheme.createButton("Add", UITheme.ACCENT_GREEN, UITheme.ACCENT_GREEN_HOVER);
        JButton updateBtn = UITheme.createButton("Update", UITheme.ACCENT_ORANGE, UITheme.ACCENT_ORANGE.brighter());
        JButton deleteBtn = UITheme.createButton("Delete", UITheme.ACCENT_RED, UITheme.ACCENT_RED_HOVER);
        JButton searchBtn = UITheme.createButton("Search", UITheme.ACCENT_BLUE, UITheme.ACCENT_BLUE_HOVER);
        JButton clearBtn = UITheme.createButton("Clear", Color.GRAY, Color.DARK_GRAY);
        JButton viewAllBtn = UITheme.createButton("View All", UITheme.PRIMARY, UITheme.ACCENT_BLUE);

        addBtn.addActionListener(e -> addDoctor());
        updateBtn.addActionListener(e -> updateDoctor());
        deleteBtn.addActionListener(e -> deleteDoctor());
        searchBtn.addActionListener(e -> searchDoctor());
        clearBtn.addActionListener(e -> clearForm());
        viewAllBtn.addActionListener(e -> loadAllDoctors());

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(searchBtn);
        buttons.add(clearBtn);
        buttons.add(viewAllBtn);

        gbc.gridy = row;
        panel.add(buttons, gbc);

        return panel;
    }

    private int addFieldComponent(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row++;
        panel.add(UITheme.createFormLabel(label), gbc);
        gbc.gridy = row++;
        panel.add(field, gbc);
        return row;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] columns = {"ID", "Name", "Specialization", "Phone", "Email", "Experience", "Available"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                populateFormFromRow(table.getSelectedRow());
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void populateFormFromRow(int row) {
        idField.setText(tableModel.getValueAt(row, 0).toString());
        nameField.setText(tableModel.getValueAt(row, 1).toString());
        specializationCombo.setSelectedItem(tableModel.getValueAt(row, 2).toString());
        phoneField.setText(tableModel.getValueAt(row, 3).toString());
        emailField.setText(safe(tableModel.getValueAt(row, 4)));
        experienceField.setText(tableModel.getValueAt(row, 5).toString());
        availableCombo.setSelectedItem(tableModel.getValueAt(row, 6).toString());
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    // ---------------- CRUD actions ----------------

    private void addDoctor() {
        Doctor doctor = buildDoctorFromForm(false);
        if (doctor == null) {
            return;
        }
        try {
            doctorDAO.addDoctor(doctor);
            JOptionPane.showMessageDialog(this, "Doctor added successfully.");
            clearForm();
            loadAllDoctors();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding doctor:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDoctor() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a doctor from the table first.",
                    "No Doctor Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Doctor doctor = buildDoctorFromForm(true);
        if (doctor == null) {
            return;
        }
        try {
            doctorDAO.updateDoctor(doctor);
            JOptionPane.showMessageDialog(this, "Doctor updated successfully.");
            clearForm();
            loadAllDoctors();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating doctor:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDoctor() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a doctor from the table first.",
                    "No Doctor Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this doctor? This will also remove their appointments.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            doctorDAO.deleteDoctor(id);
            JOptionPane.showMessageDialog(this, "Doctor deleted successfully.");
            clearForm();
            loadAllDoctors();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid doctor ID.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting doctor:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchDoctor() {
        String keyword = JOptionPane.showInputDialog(this, "Enter specialization to search:");
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        try {
            List<Doctor> results = doctorDAO.searchBySpecialization(keyword.trim());
            fillTable(results);
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No doctors found for \"" + keyword + "\".");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching doctors:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllDoctors() {
        try {
            fillTable(doctorDAO.getAllDoctors());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading doctors:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillTable(List<Doctor> doctors) {
        tableModel.setRowCount(0);
        for (Doctor d : doctors) {
            tableModel.addRow(new Object[]{
                    d.getDoctorId(), d.getName(), d.getSpecialization(), d.getPhone(),
                    d.getEmail(), d.getExperience(), d.availableAsDbString()
            });
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        specializationCombo.setSelectedIndex(0);
        phoneField.setText("");
        emailField.setText("");
        experienceField.setText("");
        availableCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private Doctor buildDoctorFromForm(boolean isUpdate) {
        String name = nameField.getText().trim();
        String specialization = String.valueOf(specializationCombo.getSelectedItem()).trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String experienceText = experienceField.getText().trim();
        boolean available = "YES".equals(availableCombo.getSelectedItem());

        if (name.isEmpty()) {
            showValidationError("Name cannot be empty.");
            return null;
        }
        if (specialization.isEmpty()) {
            showValidationError("Specialization cannot be empty.");
            return null;
        }
        if (phone.isEmpty() || !phone.matches("\\d{7,15}")) {
            showValidationError("Phone must contain 7 to 15 digits only.");
            return null;
        }
        if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            showValidationError("Email format looks invalid.");
            return null;
        }

        int experience;
        try {
            experience = Integer.parseInt(experienceText);
            if (experience < 0 || experience > 70) {
                showValidationError("Experience must be a realistic number of years.");
                return null;
            }
        } catch (NumberFormatException ex) {
            showValidationError("Experience must be numeric.");
            return null;
        }

        Doctor doctor = new Doctor();
        if (isUpdate) {
            doctor.setDoctorId(Integer.parseInt(idField.getText().trim()));
        }
        doctor.setName(name);
        doctor.setSpecialization(specialization);
        doctor.setPhone(phone);
        doctor.setEmail(email.isEmpty() ? null : email);
        doctor.setExperience(experience);
        doctor.setAvailable(available);
        return doctor;
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}
