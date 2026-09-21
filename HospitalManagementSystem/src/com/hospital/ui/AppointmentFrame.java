package com.hospital.ui;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


public class AppointmentFrame extends JFrame {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private JTextField idField, dateField, timeField, reasonField;
    private JComboBox<Patient> patientCombo;
    private JComboBox<Doctor> doctorCombo;
    private JComboBox<String> statusCombo;
    private JTable table;
    private DefaultTableModel tableModel;

    public AppointmentFrame() {
        setTitle("Appointment Management - CityCare HMS");
        setSize(1080, 620);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);

        root.add(UITheme.createHeader("Appointment Management"), BorderLayout.NORTH);
        root.add(buildFormPanel(), BorderLayout.WEST);
        root.add(buildTablePanel(), BorderLayout.CENTER);

        loadComboBoxes();
        loadAllAppointments();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(340, 0));

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

        doctorCombo = new JComboBox<>();
        doctorCombo.setFont(UITheme.FONT_FIELD);

        dateField = UITheme.createTextField();
        dateField.setText(LocalDate.now().format(DATE_FORMAT));

        timeField = UITheme.createTextField();
        timeField.setText("09:00");

        reasonField = UITheme.createTextField();

        statusCombo = new JComboBox<>(new String[]{"SCHEDULED", "COMPLETED", "CANCELLED"});
        statusCombo.setFont(UITheme.FONT_FIELD);

        row = addFieldComponent(panel, gbc, row, "Appointment ID", idField);
        row = addFieldComponent(panel, gbc, row, "Patient *", patientCombo);
        row = addFieldComponent(panel, gbc, row, "Doctor *", doctorCombo);
        row = addFieldComponent(panel, gbc, row, "Date (yyyy-MM-dd) *", dateField);
        row = addFieldComponent(panel, gbc, row, "Time (HH:mm) *", timeField);
        row = addFieldComponent(panel, gbc, row, "Reason", reasonField);
        row = addFieldComponent(panel, gbc, row, "Status", statusCombo);

        JPanel buttons = new JPanel(new GridLayout(3, 2, 8, 8));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton bookBtn = UITheme.createButton("Book", UITheme.ACCENT_GREEN, UITheme.ACCENT_GREEN_HOVER);
        JButton updateBtn = UITheme.createButton("Update", UITheme.ACCENT_ORANGE, UITheme.ACCENT_ORANGE.brighter());
        JButton cancelBtn = UITheme.createButton("Cancel Appt.", UITheme.ACCENT_RED, UITheme.ACCENT_RED_HOVER);
        JButton searchBtn = UITheme.createButton("Search", UITheme.ACCENT_BLUE, UITheme.ACCENT_BLUE_HOVER);
        JButton clearBtn = UITheme.createButton("Clear", Color.GRAY, Color.DARK_GRAY);
        JButton viewAllBtn = UITheme.createButton("View All", UITheme.PRIMARY, UITheme.ACCENT_BLUE);

        bookBtn.addActionListener(e -> bookAppointment());
        updateBtn.addActionListener(e -> updateAppointment());
        cancelBtn.addActionListener(e -> cancelAppointment());
        searchBtn.addActionListener(e -> searchAppointment());
        clearBtn.addActionListener(e -> clearForm());
        viewAllBtn.addActionListener(e -> loadAllAppointments());

        buttons.add(bookBtn);
        buttons.add(updateBtn);
        buttons.add(cancelBtn);
        buttons.add(searchBtn);
        buttons.add(clearBtn);
        buttons.add(viewAllBtn);

        gbc.gridy = row++;
        panel.add(buttons, gbc);

        JButton todayBtn = UITheme.createButton("Today's Appointments", UITheme.PRIMARY_DARK, UITheme.ACCENT_BLUE);
        todayBtn.addActionListener(e -> loadTodaysAppointments());
        gbc.gridy = row;
        panel.add(todayBtn, gbc);

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

        String[] columns = {"Appt. ID", "Patient", "Doctor", "Specialization", "Date", "Time", "Reason", "Status"};
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

    private void loadComboBoxes() {
        try {
            patientCombo.removeAllItems();
            for (Patient p : patientDAO.getAllPatients()) {
                patientCombo.addItem(p);
            }
            doctorCombo.removeAllItems();
            for (Doctor d : doctorDAO.getAllDoctors()) {
                doctorCombo.addItem(d);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading patients/doctors:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
                patientCombo.setRenderer(new DefaultListCellRendererForPatient());
    }

 
    private void populateFormFromRow(int row) {
        idField.setText(tableModel.getValueAt(row, 0).toString());
        selectComboByName(patientCombo, tableModel.getValueAt(row, 1).toString());
        selectComboByName(doctorCombo, tableModel.getValueAt(row, 2).toString());
        dateField.setText(tableModel.getValueAt(row, 4).toString());
        timeField.setText(tableModel.getValueAt(row, 5).toString());
        reasonField.setText(safe(tableModel.getValueAt(row, 6)));
        statusCombo.setSelectedItem(tableModel.getValueAt(row, 7).toString());
    }

    private void selectComboByName(JComboBox<?> combo, String name) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            String itemName = (item instanceof Patient) ? ((Patient) item).getName()
                    : (item instanceof Doctor) ? ((Doctor) item).getName() : String.valueOf(item);
            if (itemName.equals(name)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    // ---------------- Actions ----------------

    private void bookAppointment() {
        Appointment appointment = buildAppointmentFromForm(false);
        if (appointment == null) {
            return;
        }
        try {
            appointmentDAO.bookAppointment(appointment);
            JOptionPane.showMessageDialog(this, "Appointment booked successfully.");
            clearForm();
            loadAllAppointments();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error booking appointment:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAppointment() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an appointment from the table first.",
                    "No Appointment Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Appointment appointment = buildAppointmentFromForm(true);
        if (appointment == null) {
            return;
        }
        try {
            appointmentDAO.updateAppointment(appointment);
            JOptionPane.showMessageDialog(this, "Appointment updated successfully.");
            clearForm();
            loadAllAppointments();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating appointment:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelAppointment() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an appointment from the table first.",
                    "No Appointment Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel this appointment?",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            appointmentDAO.cancelAppointment(id);
            JOptionPane.showMessageDialog(this, "Appointment cancelled.");
            clearForm();
            loadAllAppointments();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error cancelling appointment:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchAppointment() {
        String keyword = JOptionPane.showInputDialog(this,
                "Enter patient name, doctor name, or status to search:");
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        try {
            fillTable(appointmentDAO.searchAppointments(keyword.trim()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching appointments:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllAppointments() {
        try {
            fillTable(appointmentDAO.getAllAppointments());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading appointments:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTodaysAppointments() {
        try {
            fillTable(appointmentDAO.getTodaysAppointments());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading today's appointments:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillTable(List<Appointment> appointments) {
        tableModel.setRowCount(0);
        for (Appointment a : appointments) {
            tableModel.addRow(new Object[]{
                    a.getAppointmentId(), a.getPatientName(), a.getDoctorName(), a.getSpecialization(),
                    a.getAppointmentDate(), a.getAppointmentTime(), a.getReason(), a.getStatus()
            });
        }
    }

    private void clearForm() {
        idField.setText("");
        if (patientCombo.getItemCount() > 0) {
            patientCombo.setSelectedIndex(0);
        }
        if (doctorCombo.getItemCount() > 0) {
            doctorCombo.setSelectedIndex(0);
        }
        dateField.setText(LocalDate.now().format(DATE_FORMAT));
        timeField.setText("09:00");
        reasonField.setText("");
        statusCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private Appointment buildAppointmentFromForm(boolean isUpdate) {
        Patient patient = (Patient) patientCombo.getSelectedItem();
        Doctor doctor = (Doctor) doctorCombo.getSelectedItem();
        String dateText = dateField.getText().trim();
        String timeText = timeField.getText().trim();
        String reason = reasonField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();

        if (patient == null || doctor == null) {
            showValidationError("Please select both a patient and a doctor.");
            return null;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateText, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            showValidationError("Date must be in yyyy-MM-dd format.");
            return null;
        }

        LocalTime time;
        try {
            time = LocalTime.parse(timeText, TIME_FORMAT);
        } catch (DateTimeParseException ex) {
            showValidationError("Time must be in HH:mm (24-hour) format.");
            return null;
        }

        Appointment appointment = new Appointment();
        if (isUpdate) {
            appointment.setAppointmentId(Integer.parseInt(idField.getText().trim()));
        }
        appointment.setPatientId(patient.getPatientId());
        appointment.setDoctorId(doctor.getDoctorId());
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setReason(reason);
        appointment.setStatus(status);
        return appointment;
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
