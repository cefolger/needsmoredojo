package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.chrisfolger.needsmoredojo.core.amd.AMDImportOrganizer;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;

public class OrganizeAMDImportsAction extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());

        DefineResolver resolver = new DefineResolver();
        final List<PsiElement> parameters = new ArrayList<PsiElement>();
        final List<PsiElement> defines = new ArrayList<PsiElement>();

        resolver.gatherDefineAndParameters(psiFile, defines, parameters);

        final AMDImportOrganizer organizer = new AMDImportOrganizer();

        CommandProcessor.getInstance().executeCommand(psiFile.getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        final AMDImportOrganizer.SortingResult result = organizer.sortDefinesAndParameters(defines, parameters);
                        organizer.reorder(defines.toArray(new PsiElement[]{}), result.getDefines(), true, result);
                        organizer.reorder(parameters.toArray(new PsiElement[]{}), result.getParameters(), false, result);
                        Notifications.Bus.notify(new Notification("needsmoredojo", "Organize AMD Imports", "Completed", NotificationType.INFORMATION));
                    }
                });
            }
        },
        "Organize AMD Imports",
        "Organize AMD Imports");
    }
}
