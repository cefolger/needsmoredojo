package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.naming.MismatchedImportsDetector;
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

/**
 * this quickfix is designed to make a parameter match its corresponding define literal.
 *
 * It's hard to do the reverse though because there usually the define literal corresponds to a file,
 * but the parameter doesn't. So for example dojo/dom-construct -> domConstruct, it's not easy to say domConstruct
 * corresponds to dom-construct.
 */
public class SwapImportsQuickFix implements LocalQuickFix {
    private LinkedHashMap<String, String> amdImportNamingExceptions = null;
    private MismatchedImportsDetector.Mismatch first;
    private MismatchedImportsDetector.Mismatch second;

    public SwapImportsQuickFix(MismatchedImportsDetector.Mismatch first, MismatchedImportsDetector.Mismatch second) {
        this.first = first;
        this.second = second;

        // TODO amdImportNamingExceptions = ServiceManager.getService(define.getProject(), DojoSettings.class).getAmdImportNamingExceptions();
    }

    @NotNull
    @Override
    public String getName() {
        return "Swap imports: " + first.getDefine().getText() + " with " + second.getDefine().getText();
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Needs More Dojo";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {


    }
}
