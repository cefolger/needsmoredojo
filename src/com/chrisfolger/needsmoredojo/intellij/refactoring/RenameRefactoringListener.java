package com.chrisfolger.needsmoredojo.intellij.refactoring;

import com.intellij.psi.PsiElement;
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

    @Override
    public void elementRenamed(@NotNull PsiElement psiElement) {
    }
}
