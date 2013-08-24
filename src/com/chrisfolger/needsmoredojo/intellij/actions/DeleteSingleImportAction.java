package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.AMDImport;
import com.chrisfolger.needsmoredojo.core.amd.AMDImportLocator;
import com.chrisfolger.needsmoredojo.core.amd.UnusedImportsRemover;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.Set;

public class DeleteSingleImportAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());

        final AMDImport amdImport = new AMDImportLocator().findNearestImport(element, psiFile);

        CommandProcessor.getInstance().executeCommand(psiFile.getProject(), new Runnable() {
            @Override
            public void run() {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                // TODO null check
                new UnusedImportsRemover().removeSingleImport(amdImport);

                 //   Notifications.Bus.notify(new Notification("needsmoredojo", "Remove Unused Imports", result.getDeletedElementNames(), NotificationType.INFORMATION));
                }
            });
            }
        },
        "Remove Import",
        "Remove Import");


    }
}
