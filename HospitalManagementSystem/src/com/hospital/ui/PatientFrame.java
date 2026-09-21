package com.hospital.ui;

import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


public class PatientFrame extends JFrame {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PatientDAO patientDAO = new PatientDAO();

    private JTextField idField, nameField, ageField, phoneField, addressField, diseaseField, bloodGroupField, admissionDateField;
    private JComboBox<String> genderCombo;
    private JTable table;
    private DefaultTableModel tableModel;

    public PatientFrame() {
        setTitle("Patient Management - CityCare HMS");
        setSize(1000, 620);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);

        root.add(UITheme.createHeader("Patient Management"), BorderLayout.NORTH);
        root.add(buildFormPanel(), BorderLayout.WEST);
        root.add(buildTablePanel(), BorderLayout.CENTER);

        loadAllPatients();
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
        ageField = UITheme.createTextField();
        phoneField = UITheme.createTextField();
        addressField = UITheme.createTextField();
        diseaseField = UITheme.createTextField();
        bloodGroupField = UITheme.createTextField();
        admissionDateField = UITheme.createTextField();
        admissionDateField.setText(LocalDate.now().format(DATE_FORMAT));
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setFont(UITheme.FONT_FIELD);

        row = addField(panel, gbc, row, "Patient ID", idField);
        row = addField(panel, gbc, row, "Name *", nameField);
        row = addField(panel, gbc, row, "Age *", ageField);
        row = addFieldComponent(panel, gbc, row, "Gender *", genderCombo);
        row = addField(panel, gbc, row, "Phone *", phoneField);
        row = addField(panel, gbc, row, "Address", addressField);
        row = addField(panel, gbc, row, "Disease", diseaseField);
        row = addField(panel, gbc, row, "Blood Group", bloodGroupField);
        row = addField(panel, gbc, row, "Admission Date (yyyy-MM-dd)", admissionDateField);

        JPanel buttons = new JPanel(new GridLayout(3, 2, 8, 8));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton addBtn = UITheme.createButton("Add", UITheme.ACCENT_GREEN, UITheme.ACCENT_GREEN_HOVER);
        JButton updateBtn = UITheme.createButton("Update", UITheme.ACCENT_ORANGE, UITheme.ACCENT_ORANGE.brighter());
        JButton deleteBtn = UITheme.createButton("Delete", UITheme.ACCENT_RED, UITheme.ACCENT_RED_HOVER);
        JButton searchBtn = UITheme.createButton("Search", UITheme.ACCENT_BLUE, UITheme.ACCENT_BLUE_HOVER);
        JButton clearBtn = UITheme.createButton("Clear", Color.GRAY, Color.DARK_GRAY);
        JButton viewAllBtn = UITheme.createButton("View All", UITheme.PRIMARY, UITheme.ACCENT_BLUE);

        addBtn.addActionListener(e -> addPatient());
        updateBtn.addActionListener(e -> updatePatient());
        deleteBtn.addActionListener(e -> deletePatient());
        searchBtn.addActionListener(e -> searchPatient());
        clearBtn.addActionListener(e -> clearForm());
        viewAllBtn.addActionListener(e -> loadAllPatients());

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

    private int addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        return addFieldComponent(panel, gbc, row, label, field);
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

        String[] columns = {"ID", "Name", "Age", "Gender", "Phone", "Address", "Disease", "Blood Group", "Admission Date"};
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
        ageField.setText(tableModel.getValueAt(row, 2).toString());
        genderCombo.setSelectedItem(tableModel.getValueAt(row, 3).toString());
        phoneField.setText(tableModel.getValueAt(row, 4).toString());
        addressField.setText(safe(tableModel.getValueAt(row, 5)));
        diseaseField.setText(safe(tableModel.getValueAt(row, 6)));
        bloodGroupField.setText(safe(tableModel.getValueAt(row, 7)));
        admissionDateField.setText(tableModel.getValueAt(row, 8).toString());
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    // ---------------- CRUD actions ----------------

    private void addPatient() {
        Patient patient = buildPatientFromForm(false);
        if (patient == null) {
            return;
        }
        try {
            patientDAO.addPatient(patient);
            JOptionPane.showMessageDialog(this, "Patient added successfully.");
            clearForm();
            loadAllPatients();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding patient:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePatient() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a patient from the table first.",
                    "No Patient Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Patient patient = buildPatientFromForm(true);
        if (patient == null) {
            return;
        }
        try {
            boolean updated = patientDAO.updatePatient(patient);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Patient updated successfully.");
                clearForm();
                loadAllPatients();
            } else {
                JOptionPane.showMessageDialog(this, "No patient was updated. It may have been deleted.",
                        "Update Failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating patient:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePatient() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a patient from the table first.",
                    "No Patient Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this patient? This will also remove their appointments and bills.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            patientDAO.deletePatient(id);
            JOptionPane.showMessageDialog(this, "Patient deleted successfully.");
            clearForm();
            loadAllPatients();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid patient ID.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting patient:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchPatient() {
        String keyword = JOptionPane.showInputDialog(this, "Enter patient name to search:");
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        try {
            List<Patient> results = patientDAO.searchByName(keyword.trim());
            fillTable(results);
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No patients found matching \"" + keyword + "\".");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching patients:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllPatients() {
        try {
            fillTable(patientDAO.getAllPatients());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading patients:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillTable(List<Patient> patients) {
        tableModel.setRowCount(0);
        for (Patient p : patients) {
            tableModel.addRow(new Object[]{
                    p.getPatientId(), p.getName(), p.getAge(), p.getGender(), p.getPhone(),
                    p.getAddress(), p.getDisease(), p.getBloodGroup(), p.getAdmissionDate()
            });
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        ageField.setText("");
        genderCombo.setSelectedIndex(0);
        phoneField.setText("");
        addressField.setText("");
        diseaseField.setText("");
        bloodGroupField.setText("");
        admissionDateField.setText(LocalDate.now().format(DATE_FORMAT));
        table.clearSelection();
    }

    /**
     * Validates form input and builds a Patient object.
     * Returns null (after showing an error dialog) if validation fails.
     */
    private Patient buildPatientFromForm(boolean isUpdate) {
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String disease = diseaseField.getText().trim();
        String bloodGroup = bloodGroupField.getText().trim();
        String dateText = admissionDateField.getText().trim();

        if (name.isEmpty()) {
            showValidationError("Name cannot be empty.");
            return null;
        }
        if (phone.isEmpty() || !phone.matches("\\d{7,15}")) {
            showValidationError("Phone must contain 7 to 15 digits only.");
            return null;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0 || age > 150) {
                showValidationError("Age must be a realistic number between 1 and 150.");
                return null;
            }
        } catch (NumberFormatException ex) {
            showValidationError("Age must be numeric.");
            return null;
        }

        LocalDate admissionDate;
        try {
            admissionDate = LocalDate.parse(dateText, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            showValidationError("Admission date must be in yyyy-MM-dd format.");
            return null;
        }

        Patient patient = new Patient();
        if (isUpdate) {
            patient.setPatientId(Integer.parseInt(idField.getText().trim()));
        }
        patient.setName(name);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setPhone(phone);
        patient.setAddress(address);
        patient.setDisease(disease);
        patient.setBloodGroup(bloodGroup);
        patient.setAdmissionDate(admissionDate);
        return patient;
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}
