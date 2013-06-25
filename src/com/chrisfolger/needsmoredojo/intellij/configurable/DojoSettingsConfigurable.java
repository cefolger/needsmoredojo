package com.chrisfolger.needsmoredojo.intellij.configurable;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.AMDUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DojoSettingsConfigurable implements Configurable {
    private JComponent myComponent;
    private JButton autoDetect;
    private JPanel myPanel;
    private TextFieldWithBrowseButton projectSourcesText;
    private TextFieldWithBrowseButton dojoSourcesText;
    private JTable moduleExceptionsTable;
    private Project project;
    private String dojoSourceString;
    private String projectSourceString;

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

            projectSourceString = projectSourcesText.getText().replace('\\', '/');;
            projectSourcesText.setText(projectSourceString);
        }
    }

    private class AutoDetectDojoSources implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            PsiDirectory directory = AMDUtil.getDojoSourcesDirectory(project);
            dojoSourcesText.setText(directory.getVirtualFile().getCanonicalPath());
            dojoSourceString = directory.getVirtualFile().getCanonicalPath();
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

            dojoSourceString = dojoSourcesText.getText().replace('\\', '/');
            dojoSourcesText.setText(dojoSourceString);
        }
    }

    public JComponent createComponent() {
        myComponent = (JComponent) myPanel;
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();

        projectSourcesText.addBrowseFolderListener(null, new ProjectSourcesChosen("Project Sources", "Select the root of your project's sources to support certain features of Needs More Dojo", descriptor));
        dojoSourcesText.addBrowseFolderListener(null, new DojoSourcesChosen("Dojo Sources", "Select the root of the dojo library sources to support certain features of Needs More Dojo", descriptor));

        autoDetect.addActionListener(new AutoDetectDojoSources());

        // don't know how else to get the current project???
        DataContext context = DataManager.getInstance().getDataContext();
        Project project = DataKeys.PROJECT.getData(context);
        this.project = project;

        dojoSourceString = DojoSettings.getInstance().getDojoSourcesDirectory(project);
        dojoSourcesText.setText(dojoSourceString);

        projectSourceString = DojoSettings.getInstance().getProjectSourcesDirectory(project);
        projectSourcesText.setText(projectSourceString);

        ExceptionsTableBuilder builder = new ExceptionsTableBuilder(moduleExceptionsTable);

        return myComponent;
    }

    public Icon getIcon()
    {
        return null;
    }

    public void apply()
    {
        if(dojoSourceString != null)
        {
            DojoSettings.getInstance().setDojoSourcesDirectory(project, dojoSourceString);
        }

        if(projectSourceString != null)
        {
            DojoSettings.getInstance().setProjectSourcesDirectory(project, projectSourceString);
        }
    }

    public void disposeUIResources() {

    }

    public String getHelpTopic() {
        return "";
    }

    public void reset()
    {
        dojoSourceString = DojoSettings.getInstance().getDojoSourcesDirectory(project);
        dojoSourcesText.setText(dojoSourceString);

        projectSourceString = DojoSettings.getInstance().getProjectSourcesDirectory(project);
        projectSourcesText.setText(projectSourceString);
    }
}
