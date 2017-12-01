package com.chrisfolger.needsmoredojo.intellij.configurable;

import com.chrisfolger.needsmoredojo.core.amd.autodetection.SourcesAutoDetector;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;

public class DojoSettingsConfigurable implements Configurable {
    private JComponent myComponent;
    private JButton autoDetectDojoSources;
    private JPanel myPanel;
    private TextFieldWithBrowseButton projectSourcesText;
    private TextFieldWithBrowseButton dojoSourcesText;
    private JTable moduleExceptionsTable;
    private JButton addMapping;
    private JTextField addModuleText;
    private JTextField addParameterText;
    private JButton removeMapping;
    private JCheckBox preferRelativePathsWhenCheckBox;
    private JButton autoDetectProjectSources;
    private JTable removeUnusedImportExceptionsTable;
    private JTextField addRUIExceptionModuleText;
    private JTextField addRUIExceptionParameterText;
    private JButton addRUIModule;
    private JButton removeRUIModule;
    private JCheckBox dojoSourcesIsTheSame;
    private JCheckBox pluginEnabled;
    private JCheckBox addModulesIfThereAreNoneDetected;
    private JCheckBox allowCaseInsensitiveSearch;
    private JTextField supportedFileTypes;
    private JCheckBox displayWarningIfSourcesCheckBox;
    private JCheckBox enableRefactoringSupportForCheckBox;
    private JRadioButton singleQuotesRadioButton;
    private JRadioButton doubleQuotesRadioButton;
    private JTextField importBlockNames;
    private ButtonGroup quotesButtonGroup;
    private Project project;
    private String dojoSourceString;
    private String projectSourceString;
    private DojoSettings settingsService;
    private boolean modified = false;
    private boolean initialized = false;

    public String getDisplayName() {
        return "Needs More Dojo";
    }

    public boolean isModified() {
        return modified;
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
            autoDetectDojoSources.setEnabled(false);
            VirtualFile directory = SourcesLocator.getDojoSourcesDirectory(project, false);

            if(directory == null)
            {
                Messages.showInfoMessage("Could not find any dojo sources via auto-detection", "Auto-detect Dojo Sources");
                autoDetectDojoSources.setEnabled(true);
                return;
            }

            dojoSourcesText.setText(directory.getCanonicalPath());
            dojoSourceString = directory.getCanonicalPath();
            autoDetectDojoSources.setEnabled(true);

            updateModifiedState();
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

    private class TextChangedListener implements KeyListener
    {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {}

        @Override
        public void keyReleased(KeyEvent e) {
            updateModifiedState();
        }
    }

    private void updateModifiedState()
    {
        modified = !dojoSourcesText.getText().equals(settingsService.getDojoSourcesDirectory()) ||
                !projectSourcesText.getText().equals(settingsService.getProjectSourcesDirectory()) ||
                preferRelativePathsWhenCheckBox.isSelected() != settingsService.isPreferRelativeImports() ||
                dojoSourcesIsTheSame.isSelected() != settingsService.isDojoSourcesShareProjectSourcesRoot() ||
                pluginEnabled.isSelected() != settingsService.isNeedsMoreDojoEnabled() ||
                addModulesIfThereAreNoneDetected.isSelected() != settingsService.isAddModuleIfThereAreNoneDefined() ||
                allowCaseInsensitiveSearch.isSelected() != settingsService.isAllowCaseInsensitiveSearch() ||
                !supportedFileTypes.getText().equals(settingsService.getSupportedFileTypes()) ||
                !importBlockNames.getText().equals(settingsService.getImportBlockNames()) ||
                enableRefactoringSupportForCheckBox.isSelected() != settingsService.isRefactoringEnabled() ||
                singleQuotesRadioButton.isSelected() != settingsService.isSingleQuotedModuleIDs() ||
                // this flag is true if it's unchecked
                displayWarningIfSourcesCheckBox.isSelected() == settingsService.isSetupWarningDisabled();

    }

    public JComponent createComponent() {
        myComponent = (JComponent) myPanel;
        FileChooserDescriptor projectDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        // have to use a custom one to allow jar file contents to be selected
        FileChooserDescriptor dojoDescriptor = new FileChooserDescriptor(true, true, true, false, true, false);

        projectSourcesText.addBrowseFolderListener(null, new ProjectSourcesChosen("Project Sources", "Select the root of your project's sources to support certain features of Needs More Dojo", projectDescriptor));
        dojoSourcesText.addBrowseFolderListener(null, new DojoSourcesChosen("Dojo Sources", "Select the root of the dojo library sources to support certain features of Needs More Dojo", dojoDescriptor));

        autoDetectDojoSources.addActionListener(new AutoDetectDojoSources());

        // don't know how else to get the current project???
        DataContext context = DataManager.getInstance().getDataContext();
        final Project project = PlatformDataKeys.PROJECT.getData(context);
        this.project = project;

        settingsService = ServiceManager.getService(project, DojoSettings.class);

        dojoSourceString = settingsService.getDojoSourcesDirectory();
        dojoSourcesText.setText(dojoSourceString);

        projectSourceString =settingsService.getProjectSourcesDirectory();
        projectSourcesText.setText(projectSourceString);

        final ExceptionsListTableBuilder builder = new ExceptionsListTableBuilder(moduleExceptionsTable, project, settingsService.getAmdImportNamingExceptionsList());
        final ExceptionsTableBuilder ruiBuilder = new ExceptionsTableBuilder(removeUnusedImportExceptionsTable, project, settingsService.getRuiImportExceptions());

        addMapping.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                builder.getTableModel().addRow(new String[] { addModuleText.getText(), addParameterText.getText()});
            }
        });

        addRUIModule.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ruiBuilder.getTableModel().addRow(new String[] { addRUIExceptionModuleText.getText(), addRUIExceptionParameterText.getText()});
            }
        });

        removeRUIModule.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ruiBuilder.getTableModel().removeRow(removeUnusedImportExceptionsTable.getSelectedRow());
            }
        });

        removeMapping.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                builder.getTableModel().removeRow(moduleExceptionsTable.getSelectedRow());
            }
        });

        autoDetectProjectSources.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autoDetectProjectSources.setEnabled(false);
                addAutoDetectedSource(new SourcesAutoDetector().getPossibleSourceRoots(project));
                autoDetectProjectSources.setEnabled(true);
                updateModifiedState();
            }
        });

        preferRelativePathsWhenCheckBox.setSelected(settingsService.isPreferRelativeImports());
        dojoSourcesIsTheSame.setSelected(settingsService.isDojoSourcesShareProjectSourcesRoot());
        addModulesIfThereAreNoneDetected.setSelected(settingsService.isAddModuleIfThereAreNoneDefined());

        pluginEnabled.setSelected(settingsService.isNeedsMoreDojoEnabled());
        updatePluginEnabledUI();
        initialized = true;

        dojoSourcesText.getTextField().addKeyListener(new TextChangedListener());
        projectSourcesText.getTextField().addKeyListener(new TextChangedListener());

        addModulesIfThereAreNoneDetected.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateModifiedState();
            }
        });
        allowCaseInsensitiveSearch.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateModifiedState();
            }
        });
        enableRefactoringSupportForCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateModifiedState();
            }
        });
        displayWarningIfSourcesCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateModifiedState();
            }
        });

        preferRelativePathsWhenCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateModifiedState();
            }
        });

        supportedFileTypes.addKeyListener(new TextChangedListener());

        importBlockNames.addKeyListener(new TextChangedListener());

        pluginEnabled.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateModifiedState();
                updatePluginEnabledUI();
            }
        });

        dojoSourcesIsTheSame.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateSourcesEnabledState();
                updateModifiedState();
            }
        });

        quotesButtonGroup = new ButtonGroup();
        quotesButtonGroup.add(singleQuotesRadioButton);
        quotesButtonGroup.add(doubleQuotesRadioButton);

        singleQuotesRadioButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                updateModifiedState();
            }
        } );

        doubleQuotesRadioButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                updateModifiedState();
            }
        } );

        return myComponent;
    }

    public Icon getIcon()
    {
        return null;
    }

    private void updateSourcesEnabledState()
    {
        if(dojoSourcesIsTheSame.isSelected())
        {
            dojoSourcesText.setEnabled(false);
        }
        else
        {
            dojoSourcesText.setEnabled(true);
        }
    }

    private void addAutoDetectedSource(Collection<String> possibleSourceRoots)
    {
        String[] choices = possibleSourceRoots.toArray(new String[0]);

        if(choices.length == 0)
        {
            Messages.showInfoMessage("Could not find any source roots via auto-detection", "Auto-detect Project Sources");
            return;
        }

        String result = choices[0];
        if(choices.length > 1)
        {
            result = Messages.showEditableChooseDialog("Found these possible source roots: ", "Auto-detect Project Sources", null, choices, choices[0], null);
            if(result == null || result.equals(""))
            {
                return;
            }

            if(result.contains("(") && result.contains(")"))
            {
                result = result.substring(result.indexOf('(') + 1, result.indexOf(')'));
            }
        }
        else if (choices.length == 1)
        {
            if(result.contains("(") && result.contains(")"))
            {
                result = result.substring(result.indexOf('(') + 1, result.indexOf(')'));
            }
        }

        projectSourceString = result;
        projectSourcesText.setText(result);
    }

    public void apply()
    {
        if(dojoSourcesText.getText() != null)
        {
           settingsService.setDojoSourcesDirectory(dojoSourcesText.getText());
        }

        if(projectSourcesText.getText() != null)
        {
           settingsService.setProjectSourcesDirectory(projectSourcesText.getText());
        }

        settingsService.setPreferRelativeImports(preferRelativePathsWhenCheckBox.isSelected());
        settingsService.setDojoSourcesShareProjectSourcesRoot(dojoSourcesIsTheSame.isSelected());
        settingsService.setNeedsMoreDojoEnabled(pluginEnabled.isSelected());
        settingsService.setAddModuleIfThereAreNoneDefined(addModulesIfThereAreNoneDetected.isSelected());
        settingsService.setAllowCaseInsensitiveSearch(allowCaseInsensitiveSearch.isSelected());
        settingsService.setSupportedFileTypes(supportedFileTypes.getText());
        settingsService.setImportBlockNames(importBlockNames.getText());
        settingsService.setSetupWarningDisabled(!displayWarningIfSourcesCheckBox.isSelected());
        settingsService.setRefactoringEnabled(enableRefactoringSupportForCheckBox.isSelected());
        settingsService.setSingleQuotedModuleIDs(singleQuotesRadioButton.isSelected());

        modified = false;
    }

    public void disposeUIResources() {

    }

    public String getHelpTopic() {
        return "";
    }

    public void reset()
    {
        dojoSourceString = settingsService.getDojoSourcesDirectory();
        dojoSourcesText.setText(dojoSourceString);
        dojoSourcesText.setEnabled(!settingsService.isDojoSourcesShareProjectSourcesRoot());
        dojoSourcesIsTheSame.setSelected(settingsService.isDojoSourcesShareProjectSourcesRoot());

        projectSourceString =settingsService.getProjectSourcesDirectory();
        projectSourcesText.setText(projectSourceString);
        pluginEnabled.setSelected(settingsService.isNeedsMoreDojoEnabled());

        preferRelativePathsWhenCheckBox.setSelected(settingsService.isPreferRelativeImports());
        addModulesIfThereAreNoneDetected.setSelected(settingsService.isAddModuleIfThereAreNoneDefined());
        allowCaseInsensitiveSearch.setSelected(settingsService.isAllowCaseInsensitiveSearch());
        supportedFileTypes.setText(settingsService.getSupportedFileTypes());
        importBlockNames.setText(settingsService.getImportBlockNames());
        displayWarningIfSourcesCheckBox.setSelected(!settingsService.isSetupWarningDisabled());
        enableRefactoringSupportForCheckBox.setSelected(settingsService.isRefactoringEnabled());
        if (settingsService.isSingleQuotedModuleIDs()) {
            singleQuotesRadioButton.setSelected(true);
        } else {
            doubleQuotesRadioButton.setSelected(true);
        }

        modified = false;
    }

    private void updatePluginEnabledUI()
    {
        if(!pluginEnabled.isSelected())
        {
            for(Component component : myPanel.getComponents())
            {
                if(!component.equals(pluginEnabled))
                {
                    component.setEnabled(false);
                }
            }
        }
        else
        {
            for(Component component : myPanel.getComponents())
            {
                if(!component.equals(pluginEnabled))
                {
                    component.setEnabled(true);
                }
            }

            // reset the dojo source textbox being enabled/disabled based on whether its checked as being
            // the same as project sources
            updateSourcesEnabledState();
        }
    }
}
