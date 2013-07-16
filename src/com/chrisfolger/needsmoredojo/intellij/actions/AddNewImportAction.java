package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.ImportCreator;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class AddNewImportAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String initialChoice = "";

        if(editor != null)
        {
            PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());
            initialChoice = new ImportCreator().getSuggestedImport(element);
        }

        String importModule = Messages.showInputDialog("Enter the path to your module or the unqualified name of the dojo module", "Add new AMD import", null, initialChoice, null);

        if(importModule == null)
        {
            return;
        }

        DojoSettings settingsService = ServiceManager.getService(psiFile.getProject(), DojoSettings.class);
        String[] choices = new ImportCreator().getPossibleDojoImports(psiFile, importModule, settingsService.isPreferRelativeImports());
        // there will be always one choice (the original module)
        if(choices.length > 0)
        {
            importModule = Messages.showEditableChooseDialog("", "Add new AMD Import", null, choices, choices[0], null);
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

        CommandProcessor.getInstance().executeCommand(psiFile.getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        new ImportCreator().addImport(psiFile, importedModule);
                    }
                });
            }
        },
        "Add new AMD import",
        "Add new AMD import");
    }
}