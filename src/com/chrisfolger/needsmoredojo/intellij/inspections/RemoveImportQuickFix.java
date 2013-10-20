package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.AMDImport;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class RemoveImportQuickFix implements LocalQuickFix {
    private PsiElement define;
    private PsiElement parameter;

    public RemoveImportQuickFix(PsiElement define, PsiElement parameter) {
        this.define = define;
        this.parameter = parameter;
    }

    @NotNull
    @Override
    public String getName() {
        return "Delete " + define.getText() + " and its parameter";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Needs More Dojo";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor)
    {
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        AMDPsiUtil.removeSingleImport(new AMDImport((JSElement)define, (JSElement) parameter));
                    }
                });
            }
        },
            "Remove unused import",
            "Remove unused import");
    }
}
