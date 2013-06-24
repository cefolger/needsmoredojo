package com.chrisfolger.needsmoredojo.intellij.configurable;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;

public class DojoSettingsConfigurable implements Configurable {
    private JComponent myComponent;
    private JButton autoDetect;
    private JPanel myPanel;
    private TextFieldWithBrowseButton projectSourcesText;
    private TextFieldWithBrowseButton dojoSourcesText;

    public String getDisplayName() {
        return "Needs More Dojo";
    }

    public boolean isModified() {
        return true;
    }

    public JComponent createComponent() {
        myComponent = (JComponent) myPanel;
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();

        projectSourcesText.addBrowseFolderListener("Project Sources", "Select the root of your project sources to support certain features of Needs More Dojo", null, descriptor);
        dojoSourcesText.addBrowseFolderListener("Dojo Sources", "Select the root of the dojo library sources to support certain features of Needs More Dojo", null, descriptor);
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
