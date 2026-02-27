package com.example.core;

import java.util.*;

public class PlanParser {

    public static PlanNode parsePlan(List<String> lines) {

        if (lines == null || lines.isEmpty()) {
            return null;
        }

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

        // 3️⃣ 构造树
        PlanNode root = null;
        Deque<PlanNode> stack = new ArrayDeque<>();

        for (String line : lines) {

            if (!line.startsWith("|"))
                continue;

            if (line.contains("Operation") || line.contains("---"))
                continue;

            String idRaw = getColumn(line, columnPositions, 0);
            if (idRaw.isEmpty())
                continue;

            boolean hasPredicate = idRaw.contains("*");

            String id = idRaw.replace("*", "").trim();
            String operationRaw = getColumn(line, columnPositions, 1);
            String name = getColumn(line, columnPositions, 2);

            Map<String, Integer> columnIndexMap = new HashMap<>();

            for (int i = 0; i < columnPositions.size() - 1; i++) {
                String colName = getColumn(headerLine, columnPositions, i);
                columnIndexMap.put(colName.trim(), i);
            }

            String rows = getColumnByName(line, columnPositions, columnIndexMap, "Rows");
            String bytes = getColumnByName(line, columnPositions, columnIndexMap, "Bytes");
            String cost = getColumnByName(line, columnPositions, columnIndexMap, "Cost");
            String time = getColumnByName(line, columnPositions, columnIndexMap, "Time");
            String tq = getColumnByName(line, columnPositions, columnIndexMap, "TQ");
            String inOut = getColumnByName(line, columnPositions, columnIndexMap, "IN-OUT");
            String pqDistrib = getColumnByName(line, columnPositions, columnIndexMap, "PQ Distrib");

            // 4️⃣ 计算层级（核心稳定算法）
            int level = line.indexOf(operationRaw);

            String operation = operationRaw.trim();

            PlanNode node = new PlanNode();
            node.setId(id);
            node.setOperation(operation);
            node.setName(name);
            node.setLevel(level);
            node.setRows(rows);
            node.setBytes(bytes);
            node.setCost(cost);
            node.setTime(time);
            node.setTq(tq);
            node.setInOut(inOut);
            node.setPqDistrib(pqDistrib);
            node.setHasPredicate(hasPredicate);

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
}