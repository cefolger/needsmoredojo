package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.define.organizer.AMDImportOrganizer;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.organizer.SortingResult;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportCreator;
import com.chrisfolger.needsmoredojo.core.amd.importing.InvalidDefineException;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.intellij.lang.javascript.psi.JSCallExpression;
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

import java.util.ArrayList;
import java.util.List;

/**
 * this is an action that takes a bunch of AMD imports and organizes them alphabetically
 */
public class OrganizeAMDImportsAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());

        DefineResolver resolver = new DefineResolver();
        final List<PsiElement> parameters = new ArrayList<PsiElement>();
        final List<PsiElement> defines = new ArrayList<PsiElement>();

        boolean alreadyImported = false;
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if(editor != null)
        {
            PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());
            DefineStatement statement = resolver.getNearestImportBlock(element);

            if(statement != null)
            {
                JSCallExpression callExpression = statement.getCallExpression();
                try {
                    resolver.addDefinesAndParametersOfImportBlock(callExpression, defines, parameters);
                    alreadyImported = true;
                } catch (InvalidDefineException e1) {
                    // it's not important that we handle this
                }
            }
        }

        if(!alreadyImported)
        {
            resolver.gatherDefineAndParameters(psiFile, defines, parameters);
        }

        final AMDImportOrganizer organizer = new AMDImportOrganizer();

        CommandProcessor.getInstance().executeCommand(psiFile.getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        final SortingResult result = organizer.sortDefinesAndParameters(defines, parameters);

                        if(defines.size() == 0 || parameters.size() == 0)
                        {
                            Notifications.Bus.notify(new Notification("needsmoredojo", "Organize AMD Imports", "There were no AMD imports", NotificationType.WARNING));
                            return;
                        }

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
