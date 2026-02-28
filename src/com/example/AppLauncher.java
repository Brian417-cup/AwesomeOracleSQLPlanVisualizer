package com.example;

import com.example.ui.MainFrame;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.io.InputStream;

public class AppLauncher {

    public static void main(String[] args) {
        try {
            // 1. 加载自带字体
            InputStream fontStream = AppLauncher.class.getClassLoader().getResourceAsStream("resources/font/wqy-microhei.ttf");
            if (fontStream == null) {
                throw new RuntimeException("字体文件未找到: resources/font/wqy-microhei.ttf");
            }

            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            fontStream.close();

            // 2. 设置字号
            Font font = baseFont.deriveFont(Font.PLAIN, 18f);

            // 3. 全局替换 Swing 字体
            setUIFont(new FontUIResource(font));

            // 4. 启动界面
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("启动失败: " + e.getMessage());
        }
    }

    // 设置全局 UI 字体
    private static void setUIFont(FontUIResource font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}