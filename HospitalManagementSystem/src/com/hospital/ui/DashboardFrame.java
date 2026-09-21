package com.hospital.ui;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.RoomDAO;
import com.hospital.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;


public class DashboardFrame extends JFrame {

    private final User loggedInUser;

    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    private JLabel totalPatientsValue;
    private JLabel totalDoctorsValue;
    private JLabel todaysAppointmentsValue;
    private JLabel availableRoomsValue;

    public DashboardFrame(User loggedInUser) {
        this.loggedInUser = loggedInUser;

        setTitle("Dashboard - CityCare Hospital Management System");
        setSize(1050, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainContent(), BorderLayout.CENTER);

        refreshStatistics();
    }

    private JPanel buildHeader() {
        JPanel header = UITheme.createHeader("Dashboard");
        JLabel welcome = new JLabel("Logged in as: " + loggedInUser.getUsername()
                + " (" + loggedInUser.getRole() + ")");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        welcome.setForeground(Color.WHITE);

        JPanel east = new JPanel(new BorderLayout());
        east.setOpaque(false);
        east.add(welcome, BorderLayout.NORTH);
        header.add(east, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.PRIMARY);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        sidebar.add(sidebarButton("Patients", e -> new PatientFrame().setVisible(true)));
        sidebar.add(sidebarButton("Doctors", e -> new DoctorFrame().setVisible(true)));
        sidebar.add(sidebarButton("Appointments", e -> new AppointmentFrame().setVisible(true)));
        sidebar.add(sidebarButton("Rooms", e -> new RoomFrame().setVisible(true)));
        sidebar.add(sidebarButton("Billing", e -> new BillFrame().setVisible(true)));

        sidebar.add(Box.createVerticalGlue());

        JButton refresh = UITheme.createButton("Refresh Stats", UITheme.ACCENT_BLUE, UITheme.ACCENT_BLUE_HOVER);
        refresh.setAlignmentX(Component.CENTER_ALIGNMENT);
        refresh.setMaximumSize(new Dimension(190, 36));
        refresh.addActionListener(e -> refreshStatistics());
        sidebar.add(refresh);
        sidebar.add(Box.createVerticalStrut(10));

        JButton logout = UITheme.createButton("Logout", UITheme.ACCENT_ORANGE, UITheme.ACCENT_ORANGE.brighter());
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.setMaximumSize(new Dimension(190, 36));
        logout.addActionListener(e -> logout());
        sidebar.add(logout);
        sidebar.add(Box.createVerticalStrut(10));

        JButton exit = UITheme.createButton("Exit", UITheme.ACCENT_RED, UITheme.ACCENT_RED_HOVER);
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setMaximumSize(new Dimension(190, 36));
        exit.addActionListener(e -> System.exit(0));
        sidebar.add(exit);

        return sidebar;
    }

    private JButton sidebarButton(String text, java.awt.event.ActionListener listener) {
        JButton button = UITheme.createButton(text, UITheme.PRIMARY_DARK, UITheme.ACCENT_BLUE);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(190, 42));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.addActionListener(listener);
        Dimension spacer = new Dimension(0, 8);
        JPanel wrapper = null; // not needed, just add spacing via border
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.PRIMARY_DARK, 0), new EmptyBorder(10, 14, 10, 14)));
        return button;
    }

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UITheme.BACKGROUND);
        content.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel sectionTitle = new JLabel("Hospital Overview");
        sectionTitle.setFont(UITheme.FONT_HEADER);
        sectionTitle.setForeground(UITheme.TEXT_DARK);
        content.add(sectionTitle, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setBackground(UITheme.BACKGROUND);
        cardsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        totalPatientsValue = new JLabel("0");
        totalDoctorsValue = new JLabel("0");
        todaysAppointmentsValue = new JLabel("0");
        availableRoomsValue = new JLabel("0");

        cardsPanel.add(buildStatCard("Total Patients", totalPatientsValue, UITheme.ACCENT_BLUE));
        cardsPanel.add(buildStatCard("Total Doctors", totalDoctorsValue, UITheme.ACCENT_GREEN));
        cardsPanel.add(buildStatCard("Today's Appointments", todaysAppointmentsValue, UITheme.ACCENT_ORANGE));
        cardsPanel.add(buildStatCard("Available Rooms", availableRoomsValue, UITheme.ACCENT_RED));

        content.add(cardsPanel, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildStatCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1),
                new EmptyBorder(18, 20, 18, 20)));

        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(6, 0));
        card.add(stripe, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(new EmptyBorder(0, 16, 0, 0));

        valueLabel.setFont(UITheme.FONT_STAT_NUMBER);
        valueLabel.setForeground(UITheme.TEXT_DARK);

        JLabel captionLabel = new JLabel(label);
        captionLabel.setFont(UITheme.FONT_LABEL);
        captionLabel.setForeground(Color.GRAY);

        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(captionLabel);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private void refreshStatistics() {
        try {
            totalPatientsValue.setText(String.valueOf(patientDAO.countPatients()));
            totalDoctorsValue.setText(String.valueOf(doctorDAO.countDoctors()));
            todaysAppointmentsValue.setText(String.valueOf(appointmentDAO.countTodaysAppointments()));
            availableRoomsValue.setText(String.valueOf(roomDAO.countAvailableRooms()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load dashboard statistics.\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
