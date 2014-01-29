package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportCreator;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportResolver;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.chrisfolger.needsmoredojo.intellij.dialog.AddNewImportSelectionDialog;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * the action responsible for launching the add import dialog
 */
public class AddNewImportAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        Project project = e.getProject();
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(project);

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String initialChoice = "";
        PsiElement element = null;

        if(editor != null)
        {
            element = psiFile.findElementAt(editor.getCaretModel().getOffset());
            initialChoice = new ImportCreator().getSuggestedImport(element);
        }

        String warning = "";

        String projectSources = ServiceManager.getService(project, DojoSettings.class).getProjectSourcesDirectory();
        if(projectSources == null || projectSources.equals(""))
        {
            warning += "\n*For best results, configure your project sources in the settings dialog*";
        }

        String importModule = Messages.showInputDialog("Enter the module name or its full path" + warning, "Add new AMD import", null, initialChoice, null);

        if(importModule == null)
        {
            return;
        }

        DojoSettings settingsService = ServiceManager.getService(psiFile.getProject(), DojoSettings.class);
        String[] choices = new ImportResolver().getPossibleDojoImports(psiFile, importModule, settingsService.isPreferRelativeImports(), settingsService.isAddModuleIfThereAreNoneDefined(), settingsService.isAllowCaseInsensitiveSearch());

        if(choices.length == 1 && settingsService.isAddModuleIfThereAreNoneDefined())
        {
            // do nothing for this case
        }
        else if(choices.length > 0)
        {
            AddNewImportSelectionDialog dialog = new AddNewImportSelectionDialog(project);
            dialog.getPeer().setTitle("Add New AMD Import");
            dialog.show(choices, choices[0]);
            if(dialog.isOK())
            {
                importModule = dialog.getSelectedItem();
            }
            else
            {
                importModule = null;
            }
        }
        else if (choices.length == 0)
        {
            new Notification("needsmoredojo", "Add new Import", "No module was found to import", NotificationType.WARNING).notify(psiFile.getProject());
            return;
        }

        if(importModule == null)
        {
            return;
        }

        final String importedModule = importModule;

        if(importedModule.equals(""))
        {
            Notifications.Bus.notify(new Notification("needsmoredojo", "Add new Import", "Invalid module", NotificationType.INFORMATION));
            return;
        }

        // determine if there is a nearest define/require to add to
        DefineStatement statementToAddTo = null;
        if(element != null)
        {
            statementToAddTo = new DefineResolver().getNearestImportBlock(element);
        }

        final DefineStatement finalStatementToAddTo = statementToAddTo;
        CommandProcessor.getInstance().executeCommand(psiFile.getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {

                        boolean result;
                        if(finalStatementToAddTo != null)
                        {
                            result = new ImportCreator().addImport(psiFile, importedModule, finalStatementToAddTo);
                        }
                        else
                        {
                            result = new ImportCreator().addImport(psiFile, importedModule);
                        }
                        if(!result)
                        {
                            new Notification("needsmoredojo", "Add new Import", "A define statement was not found", NotificationType.WARNING).notify(psiFile.getProject());
                        }
                    }
                });
            }
        },
        "Add new AMD import",
        "Add new AMD import");
    }
}