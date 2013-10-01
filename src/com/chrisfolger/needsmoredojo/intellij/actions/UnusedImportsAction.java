package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.importing.UnusedImportBlockEntry;
import com.chrisfolger.needsmoredojo.core.amd.importing.UnusedImportsRemover;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *  runs when the user wants to remove all unused imports
 */
public class UnusedImportsAction extends JavaScriptAction {
    protected boolean deleteMode = false;

    @Override
    protected boolean supportsFileType(FileType type) {
        return super.supportsFileType(type) || type instanceof HtmlFileType;
    }

    public void actionPerformed(@NotNull final AnActionEvent e)
    {
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());

        final UnusedImportsRemover detector = new UnusedImportsRemover();
        final List<UnusedImportBlockEntry> results = detector.filterUsedModules(psiFile, ServiceManager.getService(psiFile.getProject(), DojoSettings.class).getRuiImportExceptions());

        if(this.deleteMode)
        {
            CommandProcessor.getInstance().executeCommand(psiFile.getProject(), new Runnable() {
                @Override
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            int numDeleted = 0;

                            for(UnusedImportBlockEntry entry : results)
                            {
                                if(entry.getDefines() == null || entry.getParameters() == null || entry.getDefines().size() == 0 || entry.getParameters().size() == 0)
                                {
                                    continue;
                                }

                                UnusedImportsRemover.RemovalResult result = detector.removeUnusedParameters(entry.getParameters(), entry.getDefines());
                                numDeleted += result.getElementsToDelete().size();

                                if(result.getElementsToDelete().size() > 0)
                                {
                                    Notifications.Bus.notify(new Notification("needsmoredojo", "Remove Unused Imports", result.getDeletedElementNames(), NotificationType.INFORMATION));
                                }
                            }

                            if(numDeleted == 0)
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
