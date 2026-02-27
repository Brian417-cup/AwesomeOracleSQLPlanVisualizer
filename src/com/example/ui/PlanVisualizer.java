package com.example.ui;

import com.example.core.PlanNode;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlanVisualizer extends JPanel {

    private JTree tree;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea executionOrderArea;

    public PlanVisualizer() {

        setLayout(new BorderLayout());

        // ===== 左边 JTree =====
        tree = new JTree(new DefaultMutableTreeNode("未加载执行计划"));

        // ===== 右边 JTable =====
        String[] columns = {"属性", "值"};

        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setDefaultEditor(Object.class, null);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        table.getColumnModel().getColumn(0).setMinWidth(130);
        table.getColumnModel().getColumn(0).setMaxWidth(150);

        JScrollPane tableScrollPane = new JScrollPane(table);

        // ===== 新增：执行顺序区域 =====
        executionOrderArea = new JTextArea();
        executionOrderArea.setEditable(false);
        executionOrderArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane orderScrollPane = new JScrollPane(executionOrderArea);

        // ===== 右侧上下分割（新增的关键）=====
        JSplitPane rightSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                tableScrollPane,
                orderScrollPane
        );

        rightSplitPane.setDividerLocation(350);   // 上面表格高度
        rightSplitPane.setResizeWeight(0.7);      // 表格优先扩展

        // ===== 整体左右分割 =====
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree),
                rightSplitPane
        );

        splitPane.setDividerLocation(400);

        add(splitPane, BorderLayout.CENTER);

        // 👇 监听树节点点击
        tree.addTreeSelectionListener(e -> onNodeSelected());
    }

    public void loadPlan(PlanNode root) {

        if (root == null) {
            tree.setModel(new DefaultTreeModel(
                    new DefaultMutableTreeNode("未加载执行计划")
            ));
            return;
        }

        DefaultMutableTreeNode swingRoot = buildTreeNode(root);
        tree.setModel(new DefaultTreeModel(swingRoot));

        expandAll();

//        右下方展示串行状态下的执行计划顺序
        generateExecutionOrder(root);
    }

    private void onNodeSelected() {

        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) path.getLastPathComponent();

        Object obj = node.getUserObject();

        if (!(obj instanceof PlanNode)) return;

        PlanNode planNode = (PlanNode) obj;

        // 清空旧数据
        tableModel.setRowCount(0);

        // 添加新数据
        addRow("Id", planNode.getId());
        addRow("Operation", planNode.getOperation());
        addRow("Name", planNode.getName());
        addRow("Rows", planNode.getRows());
        addRow("Bytes", planNode.getBytes());
        addRow("Cost", planNode.getCost());
        addRow("Time", planNode.getTime());
        addRow("TQ", planNode.getTq());
        addRow("IN-OUT", planNode.getInOut());
        addRow("PQ Distrib", planNode.getPqDistrib());
    }

    private void addRow(String key, String value) {
        if (value == null) value = "";
        tableModel.addRow(new Object[]{key, value});
    }

    private DefaultMutableTreeNode buildTreeNode(PlanNode node) {

        DefaultMutableTreeNode treeNode =
                new DefaultMutableTreeNode(node);

        for (PlanNode child : node.getChildren()) {
            treeNode.add(buildTreeNode(child));
        }

        return treeNode;
    }

    private void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

//    后序遍历顺序展示
    private void generateExecutionOrder(PlanNode root) {

        if (root == null) {
            executionOrderArea.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        List<PlanNode> orderList = new ArrayList<>();

        postOrder(root, orderList);

        sb.append("执行顺序（自底向上）：\n\n");

        int step = 1;
        for (PlanNode node : orderList) {

            sb.append(String.format("%3d. ID=%s  Operation=%s\n",
                    step++,
                    node.getId(),
                    node.getOperation()));
        }

        executionOrderArea.setText(sb.toString());
    }

    private void postOrder(PlanNode node, List<PlanNode> list) {

        for (PlanNode child : node.getChildren()) {
            postOrder(child, list);
        }

        list.add(node); // 子节点先执行
    }
}