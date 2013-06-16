package com.chrisfolger.needsmoredojo.actions;

import com.chrisfolger.needsmoredojo.base.DefineResolver;
import com.chrisfolger.needsmoredojo.refactoring.AMDImportOrganizer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
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
        final PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);

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
                    }
                });
            }
        },
        "Organize AMD Imports",
        "Organize AMD Imports");
    }
}
