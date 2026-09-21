package com.hospital.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public final class UITheme {

    public static final Color PRIMARY_DARK = new Color(0x1F2A44);   // header / sidebar
    public static final Color PRIMARY = new Color(0x2C3E50);
    public static final Color ACCENT_BLUE = new Color(0x2980B9);    // primary buttons
    public static final Color ACCENT_BLUE_HOVER = new Color(0x3498DB);
    public static final Color ACCENT_GREEN = new Color(0x27AE60);   // add / success
    public static final Color ACCENT_GREEN_HOVER = new Color(0x2ECC71);
    public static final Color ACCENT_RED = new Color(0xE74C3C);     // delete / danger
    public static final Color ACCENT_RED_HOVER = new Color(0xEC7063);
    public static final Color ACCENT_ORANGE = new Color(0xE67E22);  // update / warning
    public static final Color BACKGROUND = new Color(0xECF0F1);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(0xD0D7DE);
    public static final Color TEXT_DARK = new Color(0x2C3E50);
    public static final Color TEXT_LIGHT = Color.WHITE;

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_FIELD = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_STAT_NUMBER = new Font("Segoe UI", Font.BOLD, 28);

    private UITheme() {
    }

   
    public static JButton createButton(String text, Color base, Color hover) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(Color.WHITE);
        button.setBackground(base);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(base);
            }
        });
        return button;
    }

    public static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_DARK);
        return label;
    }

    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT_FIELD);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(5, 8, 5, 8)));
        return field;
    }

 
    public static JPanel createHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_DARK);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        JLabel hospitalLabel = new JLabel("CityCare Hospital Management System");
        hospitalLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hospitalLabel.setForeground(new Color(0xBDC3C7));
        header.add(hospitalLabel, BorderLayout.EAST);

        return header;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(FONT_FIELD);
        table.setSelectionBackground(ACCENT_BLUE);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER);
        table.getTableHeader().setFont(FONT_HEADER.deriveFont(13f));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
    }
}
