package com.chrisfolger.needsmoredojo.intellij.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;

public class AddNewImportSelectionDialog extends DialogWrapper {
    private JPanel contentPane;
    private JList importList;
    private JTextField importItem;

    public AddNewImportSelectionDialog(@Nullable Project project) {
        super(project);

        createCenterPanel();
        init();
    }


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return importList;
    }

    public String getSelectedItem()
    {
        return importItem.getText();
    }

    private String getActualItem(Object listItem)
    {
        String result = (String) listItem;
        if(result.indexOf(':') > -1)
        {
            return result.substring(2).trim();
        }
        return result;
    }

    public void show(final String[] choices, String firstChoice) {
        DefaultListModel model = new DefaultListModel();

        int selectedIndex = 0;
        for (int i = 0; i < choices.length; i++)
        {
            String prefix = "";
            if(i < 10)
            {
                prefix = i + ": ";
            }
            model.addElement(prefix + choices[i]);
            if(choices[i].equals(firstChoice))
            {
                selectedIndex = i;
            }
        }
        importList.setModel(model);
        importList.setSelectedIndex(selectedIndex);
        importItem.setText(getActualItem(importList.getSelectedValue()));

        importList.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9)
                {
                    int index = e.getKeyCode() - KeyEvent.VK_0;
                    if(index < choices.length)
                    {
                        importList.setSelectedIndex(index);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        importList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                importItem.setText(getActualItem(importList.getSelectedValue()));
            }
        });

        super.show();
    }
}
