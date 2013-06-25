package com.chrisfolger.needsmoredojo.intellij.configurable;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DojoSettingsConfigurable implements Configurable {
    private JComponent myComponent;
    private JButton autoDetect;
    private JPanel myPanel;
    private TextFieldWithBrowseButton projectSourcesText;
    private TextFieldWithBrowseButton dojoSourcesText;
    private Project project;

    public String getDisplayName() {
        return "Needs More Dojo";
    }

    public boolean isModified() {
        return true;
    }

    private class ProjectSourcesChosen extends ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>
    {
        public ProjectSourcesChosen(@Nullable String title, @Nullable String description, FileChooserDescriptor fileChooserDescriptor) {
            super(title, description, projectSourcesText, null, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            String projectSources = projectSourcesText.getText();
        }
    }

    private class DojoSourcesChosen extends ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>
    {
        public DojoSourcesChosen(@Nullable String title, @Nullable String description, FileChooserDescriptor fileChooserDescriptor) {
            super(title, description, dojoSourcesText, null, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            String dojoSources = dojoSourcesText.getText();

        }
    }

    public JComponent createComponent() {
        myComponent = (JComponent) myPanel;
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();

        projectSourcesText.addBrowseFolderListener(null, new ProjectSourcesChosen("Project Sources", "Select the root of your project's sources to support certain features of Needs More Dojo", descriptor));
        dojoSourcesText.addBrowseFolderListener(null, new DojoSourcesChosen("Dojo Sources", "Select the root of the dojo library sources to support certain features of Needs More Dojo", descriptor));

        // don't know how else to get the current project???
        DataContext context = DataManager.getInstance().getDataContext();
        Project project = DataKeys.PROJECT.getData(context);
        this.project = project;

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
