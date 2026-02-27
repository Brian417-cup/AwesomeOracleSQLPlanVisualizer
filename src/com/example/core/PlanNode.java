package com.example.core;

import java.util.ArrayList;
import java.util.List;

public class PlanNode {

    private String id = "";
    private String operation = "";
    private String name = "";
    private String rows = "";
    private String bytes = "";
    private String cost = "";
    private String time = "";
    private String tq = "";
    private String inOut = "";
    private String pqDistrib = "";
    private boolean hasPredicate;
    private int level;
    private String rawLine;

    private List<PlanNode> children = new ArrayList<>();

    public PlanNode() {

    }

    public PlanNode(int level) {
        this.level = level;
    }

    public PlanNode(int id, String operation, int level) {
        this(String.valueOf(id), operation, "", level);
    }

    public PlanNode(String id, String operation, String rawLine, int level) {
        this.id = id;
        this.operation = operation;
        this.rawLine = rawLine;
        this.level = level;
    }

    public PlanNode(String id, String operation,
                    String rawLine, int level,
                    boolean hasPredicate) {
        this.id = id;
        this.operation = operation;
        this.rawLine = rawLine;
        this.level = level;
        this.hasPredicate = hasPredicate;
    }

    public String getId() {
        return id;
    }

    public String getOperation() {
        return operation;
    }

    public String getRawLine() {
        return rawLine;
    }

    public int getLevel() {
        return level;
    }

    public List<PlanNode> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public String getRows() {
        return rows;
    }

    public String getBytes() {
        return bytes;
    }

    public String getCost() {
        return cost;
    }

    public String getTime() {
        return time;
    }

    public String getTq() {
        return tq;
    }

    public String getInOut() {
        return inOut;
    }

    public String getPqDistrib() {
        return pqDistrib;
    }

    public boolean isHasPredicate() {
        return hasPredicate;
    }

    public boolean hasPredicate() {
        return hasPredicate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

    public void setBytes(String bytes) {
        this.bytes = bytes;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTq(String tq) {
        this.tq = tq;
    }

    public void setInOut(String inOut) {
        this.inOut = inOut;
    }

    public void setPqDistrib(String pqDistrib) {
        this.pqDistrib = pqDistrib;
    }

    public void setHasPredicate(boolean hasPredicate) {
        this.hasPredicate = hasPredicate;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setRawLine(String rawLine) {
        this.rawLine = rawLine;
    }

    public void setChildren(List<PlanNode> children) {
        this.children = children;
    }

    public void addChild(PlanNode child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        return (hasPredicate ? "* " : "") + id + " - " + operation;
    }
}