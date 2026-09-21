package com.hospital.ui;

import com.hospital.dao.RoomDAO;
import com.hospital.model.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class RoomFrame extends JFrame {

    private final RoomDAO roomDAO = new RoomDAO();

    private JTextField idField, roomNumberField, priceField;
    private JComboBox<String> roomTypeCombo;
    private JComboBox<String> statusCombo;
    private JTable table;
    private DefaultTableModel tableModel;

    public RoomFrame() {
        setTitle("Room Management - CityCare HMS");
        setSize(980, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);

        root.add(UITheme.createHeader("Room Management"), BorderLayout.NORTH);
        root.add(buildFormPanel(), BorderLayout.WEST);
        root.add(buildTablePanel(), BorderLayout.CENTER);

        loadAllRooms();
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

        roomNumberField = UITheme.createTextField();
        roomTypeCombo = new JComboBox<>(new String[]{"General", "Semi-Private", "Private", "ICU", "Deluxe"});
        roomTypeCombo.setFont(UITheme.FONT_FIELD);
        roomTypeCombo.setEditable(true);
        priceField = UITheme.createTextField();
        statusCombo = new JComboBox<>(new String[]{Room.AVAILABLE, Room.OCCUPIED, Room.MAINTENANCE});
        statusCombo.setFont(UITheme.FONT_FIELD);

        row = addFieldComponent(panel, gbc, row, "Room ID", idField);
        row = addFieldComponent(panel, gbc, row, "Room Number *", roomNumberField);
        row = addFieldComponent(panel, gbc, row, "Room Type *", roomTypeCombo);
        row = addFieldComponent(panel, gbc, row, "Price / Day *", priceField);
        row = addFieldComponent(panel, gbc, row, "Status", statusCombo);

        JPanel buttons = new JPanel(new GridLayout(4, 2, 8, 8));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton addBtn = UITheme.createButton("Add", UITheme.ACCENT_GREEN, UITheme.ACCENT_GREEN_HOVER);
        JButton updateBtn = UITheme.createButton("Update", UITheme.ACCENT_ORANGE, UITheme.ACCENT_ORANGE.brighter());
        JButton deleteBtn = UITheme.createButton("Delete", UITheme.ACCENT_RED, UITheme.ACCENT_RED_HOVER);
        JButton searchBtn = UITheme.createButton("Search", UITheme.ACCENT_BLUE, UITheme.ACCENT_BLUE_HOVER);
        JButton clearBtn = UITheme.createButton("Clear", Color.GRAY, Color.DARK_GRAY);
        JButton viewAllBtn = UITheme.createButton("View All", UITheme.PRIMARY, UITheme.ACCENT_BLUE);
        JButton allocateBtn = UITheme.createButton("Allocate", UITheme.ACCENT_ORANGE, UITheme.ACCENT_ORANGE.brighter());
        JButton releaseBtn = UITheme.createButton("Release", UITheme.ACCENT_GREEN, UITheme.ACCENT_GREEN_HOVER);

        addBtn.addActionListener(e -> addRoom());
        updateBtn.addActionListener(e -> updateRoom());
        deleteBtn.addActionListener(e -> deleteRoom());
        searchBtn.addActionListener(e -> searchRoom());
        clearBtn.addActionListener(e -> clearForm());
        viewAllBtn.addActionListener(e -> loadAllRooms());
        allocateBtn.addActionListener(e -> allocateRoom());
        releaseBtn.addActionListener(e -> releaseRoom());

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(searchBtn);
        buttons.add(clearBtn);
        buttons.add(viewAllBtn);
        buttons.add(allocateBtn);
        buttons.add(releaseBtn);

        gbc.gridy = row++;
        panel.add(buttons, gbc);

        JButton availableBtn = UITheme.createButton("View Available Rooms", UITheme.PRIMARY_DARK, UITheme.ACCENT_BLUE);
        availableBtn.addActionListener(e -> loadAvailableRooms());
        gbc.gridy = row;
        panel.add(availableBtn, gbc);

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

        String[] columns = {"ID", "Room Number", "Room Type", "Price/Day", "Status"};
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
        roomNumberField.setText(tableModel.getValueAt(row, 1).toString());
        roomTypeCombo.setSelectedItem(tableModel.getValueAt(row, 2).toString());
        priceField.setText(tableModel.getValueAt(row, 3).toString());
        statusCombo.setSelectedItem(tableModel.getValueAt(row, 4).toString());
    }

    // ---------------- Actions ----------------

    private void addRoom() {
        Room room = buildRoomFromForm(false);
        if (room == null) {
            return;
        }
        try {
            if (roomDAO.roomNumberExists(room.getRoomNumber(), 0)) {
                showValidationError("Room number \"" + room.getRoomNumber() + "\" already exists.");
                return;
            }
            roomDAO.addRoom(room);
            JOptionPane.showMessageDialog(this, "Room added successfully.");
            clearForm();
            loadAllRooms();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding room:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRoom() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a room from the table first.",
                    "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Room room = buildRoomFromForm(true);
        if (room == null) {
            return;
        }
        try {
            if (roomDAO.roomNumberExists(room.getRoomNumber(), room.getRoomId())) {
                showValidationError("Another room already uses number \"" + room.getRoomNumber() + "\".");
                return;
            }
            roomDAO.updateRoom(room);
            JOptionPane.showMessageDialog(this, "Room updated successfully.");
            clearForm();
            loadAllRooms();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating room:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRoom() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a room from the table first.",
                    "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this room?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            roomDAO.deleteRoom(id);
            JOptionPane.showMessageDialog(this, "Room deleted successfully.");
            clearForm();
            loadAllRooms();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting room:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchRoom() {
        String keyword = JOptionPane.showInputDialog(this, "Enter room number to search:");
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        try {
            fillTable(roomDAO.searchByRoomNumber(keyword.trim()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching rooms:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void allocateRoom() {
        if (!requireSelection()) {
            return;
        }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            roomDAO.allocateRoom(id);
            JOptionPane.showMessageDialog(this, "Room marked as OCCUPIED.");
            clearForm();
            loadAllRooms();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error allocating room:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void releaseRoom() {
        if (!requireSelection()) {
            return;
        }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            roomDAO.releaseRoom(id);
            JOptionPane.showMessageDialog(this, "Room marked as AVAILABLE.");
            clearForm();
            loadAllRooms();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error releasing room:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean requireSelection() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a room from the table first.",
                    "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void loadAllRooms() {
        try {
            fillTable(roomDAO.getAllRooms());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading rooms:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAvailableRooms() {
        try {
            fillTable(roomDAO.getAvailableRooms());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading available rooms:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillTable(List<Room> rooms) {
        tableModel.setRowCount(0);
        for (Room r : rooms) {
            tableModel.addRow(new Object[]{
                    r.getRoomId(), r.getRoomNumber(), r.getRoomType(), r.getPricePerDay(), r.getStatus()
            });
        }
    }

    private void clearForm() {
        idField.setText("");
        roomNumberField.setText("");
        roomTypeCombo.setSelectedIndex(0);
        priceField.setText("");
        statusCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private Room buildRoomFromForm(boolean isUpdate) {
        String roomNumber = roomNumberField.getText().trim();
        String roomType = String.valueOf(roomTypeCombo.getSelectedItem()).trim();
        String priceText = priceField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();

        if (roomNumber.isEmpty()) {
            showValidationError("Room number cannot be empty.");
            return null;
        }
        if (roomType.isEmpty()) {
            showValidationError("Room type cannot be empty.");
            return null;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price < 0) {
                showValidationError("Price per day cannot be negative.");
                return null;
            }
        } catch (NumberFormatException ex) {
            showValidationError("Price per day must be numeric.");
            return null;
        }

        Room room = new Room();
        if (isUpdate) {
            room.setRoomId(Integer.parseInt(idField.getText().trim()));
        }
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setPricePerDay(price);
        room.setStatus(status);
        return room;
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}
