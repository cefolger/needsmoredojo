package com.chrisfolger.needsmoredojo.intellij.refactoring;

import com.chrisfolger.needsmoredojo.core.refactoring.ModuleReferenceLocator;
import com.chrisfolger.needsmoredojo.core.util.AMDUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jetbrains.annotations.NotNull;

public class RenameRefactoringListener implements RefactoringElementListener {
    private PsiElement originalFile = null;

    public RenameRefactoringListener(PsiElement originalFile)
    {
        this.originalFile = originalFile;
    }

    @Override
    public void elementMoved(@NotNull PsiElement psiElement) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * get all other js files IN PROJECT SOURCES
     if the file is an AMD module
     get the list of defines
     for each define
     determine if the define has a match
     if it does:
     rename it appropriately
     use a rename action to rename the reference
     */

    @Override
    public void elementRenamed(@NotNull PsiElement psiElement)
    {
        new ModuleReferenceLocator().findFilesThatReferenceModule("foo", (PsiFile) psiElement, AMDUtil.getProjectSourceDirectories(psiElement.getProject(), true));
    }
}
