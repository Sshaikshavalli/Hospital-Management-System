package com.hospital.ui;

import com.hospital.dao.BillDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Bill;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


public class BillFrame extends JFrame {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BillDAO billDAO = new BillDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    private JTextField idField, roomChargeField, doctorChargeField, medicineChargeField,
            testChargeField, totalAmountField, billDateField;
    private JComboBox<Patient> patientCombo;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel totalBilledLabel;

    public BillFrame() {
        setTitle("Billing - CityCare HMS");
        setSize(1020, 640);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);

        root.add(UITheme.createHeader("Billing"), BorderLayout.NORTH);
        root.add(buildFormPanel(), BorderLayout.WEST);
        root.add(buildTablePanel(), BorderLayout.CENTER);

        loadPatients();
        loadAllBills();
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

        patientCombo = new JComboBox<>();
        patientCombo.setFont(UITheme.FONT_FIELD);

        roomChargeField = UITheme.createTextField();
        doctorChargeField = UITheme.createTextField();
        medicineChargeField = UITheme.createTextField();
        testChargeField = UITheme.createTextField();

        totalAmountField = UITheme.createTextField();
        totalAmountField.setEditable(false);
        totalAmountField.setBackground(new Color(0xF0F0F0));
        totalAmountField.setText("0.00");

        billDateField = UITheme.createTextField();
        billDateField.setText(LocalDate.now().format(DATE_FORMAT));

        DocumentListener recalc = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                recalculateTotal();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                recalculateTotal();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                recalculateTotal();
            }
        };
        roomChargeField.getDocument().addDocumentListener(recalc);
        doctorChargeField.getDocument().addDocumentListener(recalc);
        medicineChargeField.getDocument().addDocumentListener(recalc);
        testChargeField.getDocument().addDocumentListener(recalc);

        row = addFieldComponent(panel, gbc, row, "Bill ID", idField);
        row = addFieldComponent(panel, gbc, row, "Patient *", patientCombo);
        row = addFieldComponent(panel, gbc, row, "Room Charge", roomChargeField);
        row = addFieldComponent(panel, gbc, row, "Doctor Charge", doctorChargeField);
        row = addFieldComponent(panel, gbc, row, "Medicine Charge", medicineChargeField);
        row = addFieldComponent(panel, gbc, row, "Test Charge", testChargeField);
        row = addFieldComponent(panel, gbc, row, "Total Amount", totalAmountField);
        row = addFieldComponent(panel, gbc, row, "Bill Date (yyyy-MM-dd)", billDateField);

        JPanel buttons = new JPanel(new GridLayout(3, 2, 8, 8));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton generateBtn = UITheme.createButton("Generate Bill", UITheme.ACCENT_GREEN, UITheme.ACCENT_GREEN_HOVER);
        JButton updateBtn = UITheme.createButton("Update", UITheme.ACCENT_ORANGE, UITheme.ACCENT_ORANGE.brighter());
        JButton deleteBtn = UITheme.createButton("Delete", UITheme.ACCENT_RED, UITheme.ACCENT_RED_HOVER);
        JButton searchBtn = UITheme.createButton("Search", UITheme.ACCENT_BLUE, UITheme.ACCENT_BLUE_HOVER);
        JButton clearBtn = UITheme.createButton("Clear", Color.GRAY, Color.DARK_GRAY);
        JButton viewAllBtn = UITheme.createButton("View All", UITheme.PRIMARY, UITheme.ACCENT_BLUE);

        generateBtn.addActionListener(e -> generateBill());
        updateBtn.addActionListener(e -> updateBill());
        deleteBtn.addActionListener(e -> deleteBill());
        searchBtn.addActionListener(e -> searchBill());
        clearBtn.addActionListener(e -> clearForm());
        viewAllBtn.addActionListener(e -> loadAllBills());

        buttons.add(generateBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(searchBtn);
        buttons.add(clearBtn);
        buttons.add(viewAllBtn);

        gbc.gridy = row++;
        panel.add(buttons, gbc);

        totalBilledLabel = new JLabel("Total billed (all patients): -");
        totalBilledLabel.setFont(UITheme.FONT_LABEL);
        totalBilledLabel.setForeground(UITheme.TEXT_DARK);
        gbc.gridy = row;
        panel.add(totalBilledLabel, gbc);

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

        String[] columns = {"Bill ID", "Patient", "Room", "Doctor", "Medicine", "Test", "Total", "Bill Date"};
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

    private void loadPatients() {
        try {
            patientCombo.removeAllItems();
            for (Patient p : patientDAO.getAllPatients()) {
                patientCombo.addItem(p);
            }
            patientCombo.setRenderer(new DefaultListCellRendererForPatient());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading patients:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFormFromRow(int row) {
        idField.setText(tableModel.getValueAt(row, 0).toString());
        selectPatientByName(tableModel.getValueAt(row, 1).toString());
        roomChargeField.setText(tableModel.getValueAt(row, 2).toString());
        doctorChargeField.setText(tableModel.getValueAt(row, 3).toString());
        medicineChargeField.setText(tableModel.getValueAt(row, 4).toString());
        testChargeField.setText(tableModel.getValueAt(row, 5).toString());
        totalAmountField.setText(tableModel.getValueAt(row, 6).toString());
        billDateField.setText(tableModel.getValueAt(row, 7).toString());
    }

    private void selectPatientByName(String name) {
        for (int i = 0; i < patientCombo.getItemCount(); i++) {
            if (patientCombo.getItemAt(i).getName().equals(name)) {
                patientCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void recalculateTotal() {
        double total = parseOrZero(roomChargeField.getText()) + parseOrZero(doctorChargeField.getText())
                + parseOrZero(medicineChargeField.getText()) + parseOrZero(testChargeField.getText());
        totalAmountField.setText(String.format("%.2f", total));
    }

    private double parseOrZero(String text) {
        try {
            return text.trim().isEmpty() ? 0.0 : Double.parseDouble(text.trim());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    // ---------------- Actions ----------------

    private void generateBill() {
        Bill bill = buildBillFromForm(false);
        if (bill == null) {
            return;
        }
        try {
            billDAO.generateBill(bill);
            JOptionPane.showMessageDialog(this, "Bill generated successfully. Total: "
                    + String.format("%.2f", bill.calculateTotal()));
            clearForm();
            loadAllBills();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error generating bill:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBill() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a bill from the table first.",
                    "No Bill Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Bill bill = buildBillFromForm(true);
        if (bill == null) {
            return;
        }
        try {
            billDAO.updateBill(bill);
            JOptionPane.showMessageDialog(this, "Bill updated successfully.");
            clearForm();
            loadAllBills();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating bill:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBill() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a bill from the table first.",
                    "No Bill Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this bill?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            billDAO.deleteBill(id);
            JOptionPane.showMessageDialog(this, "Bill deleted successfully.");
            clearForm();
            loadAllBills();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting bill:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchBill() {
        String keyword = JOptionPane.showInputDialog(this, "Enter patient name to search bills:");
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        try {
            fillTable(billDAO.searchByPatientName(keyword.trim()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching bills:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllBills() {
        try {
            fillTable(billDAO.getAllBills());
            double total = billDAO.getTotalBillingAmount();
            totalBilledLabel.setText("Total billed (all patients): " + String.format("%.2f", total));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading bills:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillTable(List<Bill> bills) {
        tableModel.setRowCount(0);
        for (Bill b : bills) {
            tableModel.addRow(new Object[]{
                    b.getBillId(), b.getPatientName(), b.getRoomCharge(), b.getDoctorCharge(),
                    b.getMedicineCharge(), b.getTestCharge(), b.getTotalAmount(), b.getBillDate()
            });
        }
    }

    private void clearForm() {
        idField.setText("");
        if (patientCombo.getItemCount() > 0) {
            patientCombo.setSelectedIndex(0);
        }
        roomChargeField.setText("");
        doctorChargeField.setText("");
        medicineChargeField.setText("");
        testChargeField.setText("");
        totalAmountField.setText("0.00");
        billDateField.setText(LocalDate.now().format(DATE_FORMAT));
        table.clearSelection();
    }

    private Bill buildBillFromForm(boolean isUpdate) {
        Patient patient = (Patient) patientCombo.getSelectedItem();
        if (patient == null) {
            showValidationError("Please select a patient.");
            return null;
        }

        double roomCharge, doctorCharge, medicineCharge, testCharge;
        try {
            roomCharge = parseCharge(roomChargeField.getText(), "Room charge");
            doctorCharge = parseCharge(doctorChargeField.getText(), "Doctor charge");
            medicineCharge = parseCharge(medicineChargeField.getText(), "Medicine charge");
            testCharge = parseCharge(testChargeField.getText(), "Test charge");
        } catch (NumberFormatException ex) {
            showValidationError(ex.getMessage());
            return null;
        }

        LocalDate billDate;
        try {
            billDate = LocalDate.parse(billDateField.getText().trim(), DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            showValidationError("Bill date must be in yyyy-MM-dd format.");
            return null;
        }

        Bill bill = new Bill();
        if (isUpdate) {
            bill.setBillId(Integer.parseInt(idField.getText().trim()));
        }
        bill.setPatientId(patient.getPatientId());
        bill.setRoomCharge(roomCharge);
        bill.setDoctorCharge(doctorCharge);
        bill.setMedicineCharge(medicineCharge);
        bill.setTestCharge(testCharge);
        bill.setTotalAmount(bill.calculateTotal());
        bill.setBillDate(billDate);
        return bill;
    }

    private double parseCharge(String text, String fieldName) {
        String trimmed = text.trim();
        double value = trimmed.isEmpty() ? 0.0 : Double.parseDouble(trimmed);
        if (value < 0) {
            throw new NumberFormatException(fieldName + " cannot be negative.");
        }
        return value;
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private static class DefaultListCellRendererForPatient extends javax.swing.DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            String text = (value instanceof Patient) ? ((Patient) value).getPatientId() + " - " + ((Patient) value).getName()
                    : String.valueOf(value);
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }
}
