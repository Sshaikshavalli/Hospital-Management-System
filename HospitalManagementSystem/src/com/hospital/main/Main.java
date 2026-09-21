package com.hospital.main;

import com.hospital.ui.LoginFrame;

import javax.swing.*;


public class Main {

    public static void main(String[] args) {
      
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
           
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
