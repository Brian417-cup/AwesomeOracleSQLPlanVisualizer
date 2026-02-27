package com.example;

import com.example.ui.MainFrame;

import javax.swing.*;

public class AppLauncher {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}