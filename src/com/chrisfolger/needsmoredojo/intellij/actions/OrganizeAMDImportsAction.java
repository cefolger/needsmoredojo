package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.organizer.AMDImportOrganizer;
import com.chrisfolger.needsmoredojo.core.amd.define.organizer.SortingResult;
import com.chrisfolger.needsmoredojo.core.amd.importing.InvalidDefineException;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * this is an action that takes a bunch of AMD imports and organizes them alphabetically
 */
public class OrganizeAMDImportsAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());
        final AMDImportOrganizer organizer = new AMDImportOrganizer();

        CommandProcessor.getInstance().executeCommand(psiFile.getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        int totalSize = 0;

                        DefineResolver resolver = new DefineResolver();
                        Set<JSCallExpression> expressions = resolver.getAllImportBlocks(psiFile);
                        for(JSCallExpression expression : expressions)
                        {
                            List<PsiElement> blockDefines = new ArrayList<PsiElement>();
                            List<PsiElement> blockParameters = new ArrayList<PsiElement>();

                            try {
                                resolver.addDefinesAndParametersOfImportBlock(expression, blockDefines, blockParameters);
                            } catch (InvalidDefineException e1) {}

                            if(blockDefines.size() == 0 || blockParameters.size() == 0)
                            {
                                continue;
                            }

                            SortingResult result = organizer.sortDefinesAndParameters(blockDefines, blockParameters);
                            totalSize += blockDefines.size();

                            organizer.reorder(blockDefines.toArray(new PsiElement[]{}), result.getDefines(), true, result);
                            organizer.reorder(blockParameters.toArray(new PsiElement[]{}), result.getParameters(), false, result);
                        }

                        if(totalSize == 0)
                        {
                            Notifications.Bus.notify(new Notification("needsmoredojo", "Organize AMD Imports", "There were no AMD imports", NotificationType.WARNING));
                            return;
                        }
                        else
                        {
                            Notifications.Bus.notify(new Notification("needsmoredojo", "Organize AMD Imports", "Completed", NotificationType.INFORMATION));
                        }
                    }
                });
            }
        },
        "Organize AMD Imports",
        "Organize AMD Imports");
    }
}
