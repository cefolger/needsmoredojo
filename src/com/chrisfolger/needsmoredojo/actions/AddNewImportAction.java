package com.chrisfolger.needsmoredojo.actions;

import com.chrisfolger.needsmoredojo.base.ImportCreator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.Collection;

public class AddNewImportAction extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        String importModule = Messages.showInputDialog("Enter the path to the module or name of the dojo module", "Add new AMD import", null);

        if(importModule == null)
        {
            return;
        }

        String[] choices = new ImportCreator().getPossibleDojoImports(psiFile, importModule);
        // there will be always one choice (the original module)
        if(choices.length > 1)
        {
            importModule = Messages.showEditableChooseDialog("", "Dojo imports detected", null, choices, choices[0], null);
        }

        final String importedModule = importModule;
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