package com.example.core;

import java.util.*;

/**
 * 动态列信息管理类
 * 用于存储和管理执行计划中可能出现的各种列（不在一些常用列范围内的）
 */
public class DynamicColumnInfo {

    // 固定的核心列（必须存在）
    private static final Set<String> CORE_COLUMNS = new HashSet<>(Arrays.asList(
            "Id", "Operation"
    ));

    // 已知的标准列
    private static final Set<String> STANDARD_COLUMNS = new HashSet<>(Arrays.asList(
            "Name", "Rows", "Bytes", "Cost", "Time", "TQ", "IN-OUT", "PQ Distrib"
    ));

    // 所有检测到的列名
    private Set<String> detectedColumns = new LinkedHashSet<>();

    // 列的显示顺序（核心列优先，然后是标准列，最后是其他列）
    private List<String> displayOrder = new ArrayList<>();

    public DynamicColumnInfo() {
        // 初始化显示顺序，核心列优先
        displayOrder.addAll(CORE_COLUMNS);
        displayOrder.addAll(STANDARD_COLUMNS);
    }

    /**
     * 添加检测到的列名
     */
    public void addColumn(String columnName) {
        if (columnName != null && !columnName.trim().isEmpty()) {
            String cleanName = columnName.trim();
            if (detectedColumns.add(cleanName)) {
                // 如果是新列且不在预定义顺序中，则添加到末尾
                if (!displayOrder.contains(cleanName)) {
                    displayOrder.add(cleanName);
                }
            }
        }
    }

    /**
     * 获取所有检测到的列名
     */
    public Set<String> getDetectedColumns() {
        return new HashSet<>(detectedColumns);
    }

    /**
     * 获取按显示顺序排列的列名列表
     */
    public List<String> getOrderedColumns() {
        List<String> result = new ArrayList<>();

        // 按照预定义的顺序添加已检测到的列
        for (String column : displayOrder) {
            if (detectedColumns.contains(column)) {
                result.add(column);
            }
        }

        // 添加其他未在预定义顺序中的列
        for (String column : detectedColumns) {
            if (!displayOrder.contains(column)) {
                result.add(column);
            }
        }

        return result;
    }

    /**
     * 检查是否为核心列
     */
    public boolean isCoreColumn(String columnName) {
        return CORE_COLUMNS.contains(columnName);
    }

    /**
     * 检查是否为标准列
     */
    public boolean isStandardColumn(String columnName) {
        return STANDARD_COLUMNS.contains(columnName);
    }

    /**
     * 获取列值的方法映射
     */
    public static Map<String, java.util.function.Function<PlanNode, String>> getColumnValueGetters() {
        Map<String, java.util.function.Function<PlanNode, String>> getters = new HashMap<>();

        getters.put("Id", PlanNode::getId);
        getters.put("Operation", PlanNode::getOperation);
        getters.put("Name", PlanNode::getName);
        getters.put("Rows", PlanNode::getRows);
        getters.put("Bytes", PlanNode::getBytes);
        getters.put("Cost", PlanNode::getCost);
        getters.put("Time", PlanNode::getTime);
        getters.put("TQ", PlanNode::getTq);
        getters.put("IN-OUT", PlanNode::getInOut);
        getters.put("PQ Distrib", PlanNode::getPqDistrib);

        return getters;
    }
}
