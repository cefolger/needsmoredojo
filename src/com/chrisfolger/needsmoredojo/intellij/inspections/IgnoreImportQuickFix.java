package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.importing.UnusedImportsRemover;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.chrisfolger.needsmoredojo.core.util.PsiUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
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
public class IgnoreImportQuickFix implements LocalQuickFix {
    private PsiElement define;
    private PsiElement parameter;
    private LinkedHashMap<String, String> amdImportNamingExceptions = null;

    public IgnoreImportQuickFix(PsiElement define, PsiElement parameter) {
        this.define = define;
        this.parameter = parameter;

        amdImportNamingExceptions = ServiceManager.getService(define.getProject(), DojoSettings.class).getAmdImportNamingExceptions();
    }

    @NotNull
    @Override
    public String getName() {
        return "Don't flag \"" + define.getText() + "\" as unused for this instance";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Needs More Dojo";
    }

    private void addIgnoreStatement()
    {
        PsiElement element = JSChangeUtil.createJSTreeFromText(define.getProject(), UnusedImportsRemover.IGNORE_COMMENT).getPsi();
        define.getParent().addAfter(element, define);
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
                        addIgnoreStatement();
                    }
                });
            }
        },
        "Ignore unused import",
        "Ignore unused import");
    }
}
