package com.example.ui;

import com.example.core.PlanNode;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DetailPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;

    public DetailPanel() {

        setLayout(new BorderLayout());

        // 创建表格模型
        model = new DefaultTableModel(
                new Object[]{"Field", "Value"}, 0) {

            // 禁止编辑
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);

        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void update(PlanNode node) {

        model.setRowCount(0);

        add("ID", node.getId());
        add("Operation", node.getOperation());
        add("Name", node.getName());
        add("Rows", node.getRows());
        add("Bytes", node.getBytes());
        add("Cost", node.getCost());
        add("Time", node.getTime());
        add("TQ", node.getTq());
        add("IN-OUT", node.getInOut());
        add("PQ Distrib", node.getPqDistrib());
        add("Has Predicate", node.isHasPredicate() ? "Yes" : "No");
    }

    private void add(String field, String value) {
        model.addRow(new Object[]{
                field,
                value == null ? "" : value
        });
    }
}