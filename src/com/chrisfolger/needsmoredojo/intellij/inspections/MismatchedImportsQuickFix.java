package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

public class MismatchedImportsQuickFix implements LocalQuickFix {
    private PsiElement define;
    private PsiElement parameter;

    public MismatchedImportsQuickFix(PsiElement define, PsiElement parameter) {
        this.define = define;
        this.parameter = parameter;
    }

    @NotNull
    @Override
    public String getName() {
        return "Change parameter to match define";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Needs More Dojo";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        LinkedHashMap<String, String> amdImportNamingExceptions = ServiceManager.getService(project, DojoSettings.class).getAmdImportNamingExceptions();

        final RenameRefactoring refactoring = RefactoringFactory.getInstance(project)
                .createRename(parameter, NameResolver.defineToParameter(define.getText(), amdImportNamingExceptions), false, false);

        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        refactoring.run();
                    }
                });
            }
        },
        "Rename parameter to match define",
        "Rename parameter to match define");
    }
}
