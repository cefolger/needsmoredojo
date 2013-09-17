package com.chrisfolger.needsmoredojo.intellij.toolwindows;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class FindCyclicDependenciesToolWindow
{
    public void mousePressed(MouseEvent e, Tree tree, final String results) {
        if(SwingUtilities.isRightMouseButton(e)) {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem("Copy results to clipboard");
            popup.add(menuItem);
            popup.show(tree, e.getX(), e.getY());

            menuItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mouseClicked(e);

                    Clipboard clipboard =
                            Toolkit.getDefaultToolkit().getSystemClipboard();

                    StringBuilder builder = new StringBuilder();

                    clipboard.setContents(new StringSelection(results), null);
                }
            });
        }
    }

    public void createContent(Project project, ToolWindow toolWindow, final Map<String, List<String>> modules, int numberOfPaths)
    {
        final Tree tree = new Tree();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Modules that are part of a cyclic dependency, sorted by number of occurrences. There were " + numberOfPaths + " paths with cycles");

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

        final StringBuilder result = new StringBuilder();

        for(String module : sortedKeys)
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(module + "(" + modules.get(module).size() + ")");
            result.append(node.toString() + "\n");

            for(String child : modules.get(module))
            {
                node.add(new DefaultMutableTreeNode(child));
                result.append("\t" + child + "\n");
            }
            root.add(node);
        }

        final FindCyclicDependenciesToolWindow window = this;
        DefaultTreeModel model = new DefaultTreeModel(root);
        tree.setModel(model);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                window.mousePressed(e, tree, result.toString());
            }
        });

        JScrollPane scrollPane = new JBScrollPane(tree);
        toolWindow.getComponent().add(scrollPane);
    }
}
