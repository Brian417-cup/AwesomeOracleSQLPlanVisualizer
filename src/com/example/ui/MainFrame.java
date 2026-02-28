package com.example.ui;

import com.example.core.PlanNode;
import com.example.core.PlanParser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class MainFrame extends JFrame {

    private JButton openButton;
    private JButton analyzeButton;

    private PlanVisualizer planVisualizer;
    private JTextField filePathField;
    private File selectedFile;

    public MainFrame() {

        setTitle("执行计划分析工具");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        planVisualizer = new PlanVisualizer();

        buildTopPanel();

        add(planVisualizer, BorderLayout.CENTER);
    }

    private void buildTopPanel() {

        openButton = new JButton("打开执行计划");
        analyzeButton = new JButton("分析执行计划");
        analyzeButton.setEnabled(false); // 初始不可用

        filePathField = new JTextField(40);
        filePathField.setEditable(false); // 只读
        filePathField.setBackground(Color.WHITE);

        openButton.addActionListener(e -> chooseFile());
        analyzeButton.addActionListener(e -> analyzeFile());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topPanel.add(openButton);
        topPanel.add(analyzeButton);
        topPanel.add(new JLabel("文件路径:"));
        topPanel.add(filePathField);

        add(topPanel, BorderLayout.NORTH);

        setJMenuBar(createMenuBar());
    }

    //    菜单栏
    private JMenuBar createMenuBar() {

        JMenuBar menuBar = new JMenuBar();

        JMenu helpMenu = new JMenu("帮助");

        // ===== 下载模板项 =====
        JMenuItem downloadTemplateItem = new JMenuItem("执行计划模板下载");
        downloadTemplateItem.addActionListener(e -> downloadTemplate());

        helpMenu.add(downloadTemplateItem);

        // ===== 关于项 =====
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        return menuBar;
    }

    private void downloadTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存执行计划模板");
        fileChooser.setSelectedFile(new File("plan.txt"));

        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();

            // 使用 ClassLoader 从资源路径加载 plan.txt
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("resources/template/plan.txt");

            if (inputStream == null) {
                JOptionPane.showMessageDialog(this,
                        "未找到资源文件 plan.txt！",
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }

                JOptionPane.showMessageDialog(this,
                        "模板下载成功！",
                        "成功",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "下载失败：" + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAbout() {

        StringBuilder message = new StringBuilder("");
        message.append("执行计划可视化分析工具");
        message.append("\n");
        message.append("功能：");
        message.append("\n");
        message.append("✔ 树形展示执行计划");
        message.append("\n");
        message.append("✔ 节点属性解析");
        message.append("\n");
        message.append("✔ 自动分析执行顺序");
        message.append("\n");
        message.append("✔ 支持带 * 的 Predicate 节点");
        message.append("\n");
        message.append("✔ 支持并行 TQ / IN-OUT / PQ");
        message.append("\n");
        message.append("版本：1.0");
        message.append("\n");

        JOptionPane.showMessageDialog(this,
                message,
                "关于",
                JOptionPane.INFORMATION_MESSAGE);
    }

    //    执行计划文件选择框
    private void chooseFile() {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(
                new FileNameExtensionFilter("Text Files", "txt", "sql")
        );

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            selectedFile = chooser.getSelectedFile();

            filePathField.setText(selectedFile.getAbsolutePath());

            analyzeButton.setEnabled(true); // 允许分析
        }
    }

    private void analyzeFile() {

        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this,
                    "请先选择文件",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<String> lines = Files.readAllLines(selectedFile.toPath());

            PlanNode root = PlanParser.parsePlan(lines);

            planVisualizer.loadPlan(root);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "解析失败: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}