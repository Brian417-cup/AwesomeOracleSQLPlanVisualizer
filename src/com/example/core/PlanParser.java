package com.example.core;

import java.util.*;

public class PlanParser {

    // 动态属性列管理器
    private static DynamicColumnInfo dynamicColumnInfo = new DynamicColumnInfo();

    public static PlanNode parsePlan(List<String> lines) {

        if (lines == null || lines.isEmpty()) {
            return null;
        }

        // 重置动态列信息
        dynamicColumnInfo = new DynamicColumnInfo();

        // 1️⃣ 找到标题行
        String headerLine = null;
        for (String line : lines) {
            if (line.contains("Operation") && line.contains("Id")) {
                headerLine = line;
                break;
            }
        }

        if (headerLine == null) {
            throw new RuntimeException("未找到执行计划标题行");
        }

        // 2️⃣ 计算列边界
        List<Integer> columnPositions = findColumnPositions(headerLine);

        // 3️⃣ 提取并记录所有列名
        Map<String, Integer> columnIndexMap = new HashMap<>();
        for (int i = 0; i < columnPositions.size() - 1; i++) {
            String colName = getColumn(headerLine, columnPositions, i).trim();
            if (!colName.isEmpty()) {
                columnIndexMap.put(colName, i);
                dynamicColumnInfo.addColumn(colName); // 记录列名
            }
        }

        // 4️⃣ 构造树
        PlanNode root = null;
        Deque<PlanNode> stack = new ArrayDeque<>();

        for (String line : lines) {

            if (!line.startsWith("|"))
                continue;

            if (line.contains("Operation") || line.contains("---"))
                continue;

            // 解析每行数据
            PlanNode node = new PlanNode();

            // 处理每个检测到的列
            for (Map.Entry<String, Integer> entry : columnIndexMap.entrySet()) {
                String columnName = entry.getKey();
                Integer columnIndex = entry.getValue();
                String value = getColumn(line, columnPositions, columnIndex).trim();

                // 根据列名设置对应的属性
                setNodeProperty(node, columnName, value);
            }

            // 设置层级
            String operationRaw = getColumn(line, columnPositions,
                    columnIndexMap.getOrDefault("Operation", 1));
            int level = line.indexOf(operationRaw);
            node.setLevel(level);

            if (root == null) {
                root = node;
                stack.push(node);
                continue;
            }

            while (!stack.isEmpty() && stack.peek().getLevel() >= level) {
                stack.pop();
            }

            if (!stack.isEmpty()) {
                stack.peek().addChild(node);
            }

            stack.push(node);
        }

        return root;
    }

    /**
     * 根据列名设置节点属性
     */
    private static void setNodeProperty(PlanNode node, String columnName, String value) {
        switch (columnName) {
            case "Id":
                boolean hasPredicate = value.contains("*");
                node.setId(value.replace("*", "").trim());
                node.setHasPredicate(hasPredicate);
                break;
            case "Operation":
                node.setOperation(value);
                break;
            case "Name":
                node.setName(value);
                break;
            case "Rows":
                node.setRows(value);
                break;
            case "Bytes":
                node.setBytes(value);
                break;
            case "Cost":
                node.setCost(value);
                break;
            case "Time":
                node.setTime(value);
                break;
            case "TQ":
                node.setTq(value);
                break;
            case "IN-OUT":
                node.setInOut(value);
                break;
            case "PQ Distrib":
                node.setPqDistrib(value);
                break;
            default:
                // 处理未知列，存储为动态属性
                node.setDynamicProperty(columnName, value);
                break;
        }
    }

    private static String getColumnByName(String line,
                                          List<Integer> positions,
                                          Map<String, Integer> indexMap,
                                          String columnName) {

        Integer idx = indexMap.get(columnName);
        if (idx == null) {
            return "";
        }

        return getColumn(line, positions, idx);
    }

    // ===============================
    // 计算列边界
    // ===============================
    private static List<Integer> findColumnPositions(String headerLine) {

        List<Integer> positions = new ArrayList<>();

        for (int i = 0; i < headerLine.length(); i++) {
            if (headerLine.charAt(i) == '|') {
                positions.add(i);
            }
        }

        return positions;
    }

    // ===============================
    // 按列宽截取
    // ===============================
    private static String getColumn(String line,
                                    List<Integer> positions,
                                    int columnIndex) {

        if (columnIndex >= positions.size() - 1)
            return "";

        int start = positions.get(columnIndex) + 1;
        int end = positions.get(columnIndex + 1);

        if (start >= line.length())
            return "";

        if (end > line.length())
            end = line.length();

        return line.substring(start, end).trim();
    }

    /**
     * 获取动态列信息管理器
     */
    public static DynamicColumnInfo getDynamicColumnInfo() {
        return dynamicColumnInfo;
    }
}