package com.chrisfolger.needsmoredojo.intellij.configurable;

import com.intellij.openapi.options.Configurable;

import javax.swing.*;

public class DojoSettingsConfigurable implements Configurable {
    private JComponent myComponent;
    private JButton autoDetect;
    private JPanel myPanel;
    private JTextField dojoSourcesText;
    private JTextField projectSourcesText;
    private JButton browseDojoSources;
    private JButton browseProjectSources;

    public String getDisplayName() {
        return "Needs More Dojo";
    }

    public boolean isModified() {
        return true;
    }

    public JComponent createComponent() {
        myComponent = (JComponent) myPanel;
        return myComponent;

    }

    public Icon getIcon() {
        return null;

    }

    public void apply() {
    }

    public void disposeUIResources() {

    }

    public String getHelpTopic() {
        return "";
    }

    public void reset() {

    }


}
