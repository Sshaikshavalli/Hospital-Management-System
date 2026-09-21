package com.hospital.ui;

import com.hospital.dao.UserDAO;
import com.hospital.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;


public class LoginFrame extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("CityCare Hospital Management System - Login");
        setSize(430, 430);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);

        // ---- Top banner with hospital "logo" (text-based) ----
        JPanel banner = new JPanel();
        banner.setBackground(UITheme.PRIMARY_DARK);
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(new EmptyBorder(28, 20, 28, 20));

        JLabel logo = new JLabel("+ CityCare");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Hospital Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(0xBDC3C7));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        banner.add(logo);
        banner.add(Box.createVerticalStrut(6));
        banner.add(subtitle);
        root.add(banner, BorderLayout.NORTH);

        // ---- Form ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BACKGROUND);
        form.setBorder(new EmptyBorder(30, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0;
        form.add(UITheme.createFormLabel("Username"), gbc);

        usernameField = UITheme.createTextField();
        gbc.gridy = 1;
        form.add(usernameField, gbc);

        gbc.gridy = 2;
        form.add(UITheme.createFormLabel("Password"), gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(UITheme.FONT_FIELD);
        passwordField.setBorder(usernameField.getBorder());
        gbc.gridy = 3;
        form.add(passwordField, gbc);

        // Pressing Enter in the password field triggers login
        passwordField.addActionListener(e -> attemptLogin());

        root.add(form, BorderLayout.CENTER);

        // ---- Buttons ----
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonPanel.setBackground(UITheme.BACKGROUND);
        buttonPanel.setBorder(new EmptyBorder(0, 40, 30, 40));

        JButton loginButton = UITheme.createButton("Login", UITheme.ACCENT_GREEN, UITheme.ACCENT_GREEN_HOVER);
        JButton clearButton = UITheme.createButton("Clear", UITheme.ACCENT_BLUE, UITheme.ACCENT_BLUE_HOVER);
        JButton exitButton = UITheme.createButton("Exit", UITheme.ACCENT_RED, UITheme.ACCENT_RED_HOVER);

        loginButton.addActionListener(e -> attemptLogin());
        clearButton.addActionListener(e -> clearFields());
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);
        root.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.validateLogin(username, password);
            if (user != null) {
                JOptionPane.showMessageDialog(this, "Welcome, " + user.getUsername() + "!",
                        "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                new DashboardFrame(user).setVisible(true);
                dispose(); // close LoginFrame
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not connect to the database.\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
