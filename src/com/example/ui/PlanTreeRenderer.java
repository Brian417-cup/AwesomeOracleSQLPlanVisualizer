package com.example.ui;

import com.example.core.PlanNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

class PlanTreeRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value,
            boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        Component c = super.getTreeCellRendererComponent(
                tree, value, sel, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) value;

        Object obj = node.getUserObject();

        if (obj instanceof PlanNode) {
            PlanNode plan = (PlanNode) obj;

            if (plan.hasPredicate()) {
                c.setForeground(new Color(255, 140, 0)); // 橙色
            } else
//            if (plan.getOperation().contains("FULL")) {
//                c.setForeground(Color.RED);
//            } else if (plan.getOperation().contains("JOIN")) {
//                c.setForeground(new Color(0, 102, 204));
//            } else if (plan.getOperation().contains("SORT")) {
//                c.setForeground(new Color(153, 0, 153));
//            } else
            {
                c.setForeground(Color.BLACK);
            }
        }

        return c;
    }
}