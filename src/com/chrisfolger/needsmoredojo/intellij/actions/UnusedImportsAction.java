package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.UnusedImportsRemover;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UnusedImportsAction extends JavaScriptAction {
    protected boolean deleteMode = false;

    public void actionPerformed(@NotNull final AnActionEvent e)
    {
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());

        DefineResolver resolver = new DefineResolver();
        final List<PsiElement> parameters = new ArrayList<PsiElement>();
        final List<PsiElement> defines = new ArrayList<PsiElement>();
        resolver.gatherDefineAndParameters(psiFile, defines, parameters);

        final UnusedImportsRemover detector = new UnusedImportsRemover();
        psiFile.accept(detector.getVisitorToRemoveUsedParameters(parameters, defines, ServiceManager.getService(psiFile.getProject(), DojoSettings.class).getRuiImportExceptions()));

        if(this.deleteMode)
        {
            CommandProcessor.getInstance().executeCommand(psiFile.getProject(), new Runnable() {
                @Override
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            UnusedImportsRemover.RemovalResult result = detector.removeUnusedParameters(parameters, defines);
                            Set<PsiElement> elementsToDelete = result.getElementsToDelete();

                            if(elementsToDelete.size() > 0)
                            {
                                Notifications.Bus.notify(new Notification("needsmoredojo", "Remove Unused Imports", result.getDeletedElementNames(), NotificationType.INFORMATION));
                            }
                            else
                            {
                                Notifications.Bus.notify(new Notification("needsmoredojo", "Remove Unused Imports", "No unused imports were detected to delete", NotificationType.INFORMATION));
                            }
                        }
                    });
                }
            },
            "Remove Unused Imports",
            "Remove Unused Imports");

        }
    }
}
