package com.chrisfolger.dojotools.ui;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Chris
 * Date: 12/20/12
 * Time: 7:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class DojoImportToolWindow {
    private static JPanel sharedPanel;
    private static JTextArea textArea;

    private JPanel panel;
    private JTextArea textArea1;

    public JTextArea getTextArea()
    {
        return textArea1;
    }

    public static JTextArea getSharedTextArea()
    {
        return textArea;
    }

    public static void setSharedTextArea(JTextArea shared)
    {
        textArea = shared;
    }

    public JPanel getPanel()
    {
        sharedPanel = this.panel;
        return sharedPanel;
    }

    private void createUIComponents() {
    }
}
