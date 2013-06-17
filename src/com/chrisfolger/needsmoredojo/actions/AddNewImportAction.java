package com.chrisfolger.needsmoredojo.actions;

import com.chrisfolger.needsmoredojo.base.ImportCreator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;

public class AddNewImportAction extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        final String importModule = Messages.showInputDialog("Enter the fully qualified path to the module", "Add new AMD import", null);

        if(importModule == null)
        {
            return;
        }

        CommandProcessor.getInstance().executeCommand(psiFile.getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        new ImportCreator().addImport(psiFile, importModule);
                    }
                });
            }
        },
        "Add new AMD import",
        "Add new AMD import");
    }
}