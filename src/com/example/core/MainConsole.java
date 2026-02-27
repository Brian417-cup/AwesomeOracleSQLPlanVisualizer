package com.example.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainConsole {

    //    解析执行计划文本+构建树
    public static PlanNode parsePlan(List<String> lines) {

        Stack<PlanNode> stack = new Stack<>();
        PlanNode root = null;

        for (String line : lines) {

            if (!line.startsWith("|"))
                continue;

            String[] parts = line.split("\\|");
            if (parts.length < 3)
                continue;

            String idStr = parts[1].trim().replace("*", "");
            if (!idStr.matches("\\d+"))
                continue;

            int id = Integer.parseInt(idStr);

            String opRaw = parts[2];
            int spaceCount = countLeadingSpaces(opRaw);
            int level = spaceCount / 2;

            String operation = opRaw.trim();

            PlanNode node = new PlanNode(id, operation, level);

            // 构建父子关系
            while (!stack.isEmpty() && stack.peek().getLevel() >= level) {
                stack.pop();
            }

            if (stack.isEmpty()) {
                root = node;
            } else {
                stack.peek().getChildren().add(node);
            }

            stack.push(node);
        }

        return root;
    }

    //    计算空格
    private static int countLeadingSpaces(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ' ')
                count++;
            else
                break;
        }
        return count;
    }

    // 后序遍历
    public static void postOrder(PlanNode node, List<PlanNode> result) {
        if (node == null) return;

        for (PlanNode child : node.getChildren()) {
            postOrder(child, result);
        }

        result.add(node);
    }


    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("plan.txt"));

        PlanNode root = parsePlan(lines);

        List<PlanNode> executionOrder = new ArrayList<>();
        postOrder(root, executionOrder);

        System.out.println("执行顺序：");
        int step = 1;
        for (PlanNode node : executionOrder) {
            System.out.println(step++ + " -> " + node);
        }
    }
}
