package com.example.ui;

import com.example.core.PlanNode;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PlanVisualizer extends JPanel {

    private JTree tree;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea executionOrderArea;
    private JTable executionOrderTable;
    private DefaultTableModel executionOrderTableModel;
    private List<PlanNode> executionOrderList; // 保存执行顺序列表


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

        // ===== 右下方：执行顺序区域展示 =====
        // ===== 新增：执行顺序表格 =====
        String[] orderColumns = {"执行顺序"};
        executionOrderTableModel = new DefaultTableModel(orderColumns, 0);
        executionOrderTable = new JTable(executionOrderTableModel);
        executionOrderTable.setDefaultEditor(Object.class, null); // 禁止编辑
        executionOrderTable.setRowHeight(24);
        executionOrderTable.setFillsViewportHeight(true);
        executionOrderTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane orderScrollPane = new JScrollPane(executionOrderTable);

        // ===== 右侧整体：上下分割（新增的关键）=====
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

//        右下方的表格执行顺序点击事件（绑定对树形控件对应节点选中）
        executionOrderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = executionOrderTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < executionOrderList.size()) {
                    PlanNode selectedNode = executionOrderList.get(selectedRow);
                    selectTreeNode(selectedNode);
                }
            }
        });

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

    //    树形控件节点点击事件
    private void onNodeSelected() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
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

        // 查找并高亮执行顺序表格中的对应行
        int rowIndex = executionOrderList.indexOf(planNode);
        if (rowIndex >= 0) {
            executionOrderTable.setRowSelectionInterval(rowIndex, rowIndex);
            executionOrderTable.scrollRectToVisible(executionOrderTable.getCellRect(rowIndex, 0, true));
        }
    }

    //    执行顺序表格项点击事件
    private void selectTreeNode(PlanNode targetNode) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();

        // 递归查找目标节点
        DefaultMutableTreeNode treeNode = findTreeNode(rootNode, targetNode);
        if (treeNode != null) {
            TreePath path = new TreePath(model.getPathToRoot(treeNode));
            tree.setSelectionPath(path); // 选中节点
            tree.scrollPathToVisible(path); // 滚动到可视区域
        }
    }

    // 递归查找树节点
    private DefaultMutableTreeNode findTreeNode(DefaultMutableTreeNode parent, PlanNode targetNode) {
        if (parent.getUserObject() == targetNode) {
            return parent;
        }

        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            DefaultMutableTreeNode result = findTreeNode(child, targetNode);
            if (result != null) {
                return result;
            }
        }

        return null;
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
            executionOrderTableModel.setRowCount(0); // 清空表格
            executionOrderList = new ArrayList<>(); // 清空执行顺序列表
            return;
        }

        executionOrderList = new ArrayList<>();
        postOrder(root, executionOrderList);

        // 清空旧数据
        executionOrderTableModel.setRowCount(0);

        // 填充新数据
        int step = 1;
        for (PlanNode node : executionOrderList) {
            String orderInfo = String.format("%3d. ID=%s  Operation=%s",
                    step++, node.getId(), node.getOperation());
            executionOrderTableModel.addRow(new Object[]{orderInfo});
        }
    }


    private void postOrder(PlanNode node, List<PlanNode> list) {

        for (PlanNode child : node.getChildren()) {
            postOrder(child, list);
        }

        list.add(node); // 子节点先执行
    }
}