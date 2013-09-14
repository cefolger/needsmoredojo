package com.chrisfolger.needsmoredojo.intellij.toolwindows;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.util.List;
import java.util.Map;

public class FindCyclicDependenciesToolWindow
{
    public void createContent(Project project, ToolWindow toolWindow, Map<String, List<String>> modules)
    {
        Tree tree = new Tree();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Cyclic Dependencies");
        for(String module : modules.keySet())
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(module + "(" + modules.get(module).size() + ")");
            for(String child : modules.get(module))
            {
                node.add(new DefaultMutableTreeNode(child));
            }
            root.add(node);
        }

        DefaultTreeModel model = new DefaultTreeModel(root);
        tree.setModel(model);

        JScrollPane scrollPane = new JBScrollPane(tree);

        toolWindow.getComponent().add(scrollPane);
        // TODO dispose
    }
}
