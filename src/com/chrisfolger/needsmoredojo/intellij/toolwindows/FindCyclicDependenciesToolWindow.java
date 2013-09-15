package com.chrisfolger.needsmoredojo.intellij.toolwindows;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;

public class FindCyclicDependenciesToolWindow
{
    public void createContent(Project project, ToolWindow toolWindow, final Map<String, List<String>> modules, int numberOfPaths)
    {
        Tree tree = new Tree();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Top modules that are part of a cyclic dependency. There were " + numberOfPaths + " total paths with cycles");

        List<String> sortedKeys = new ArrayList<String>();
        for(String key : modules.keySet())
        {
            sortedKeys.add(key);
        }
        Collections.sort(sortedKeys, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return modules.get(o2).size() - modules.get(o1).size();
            }
        });

        for(String module : sortedKeys)
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
