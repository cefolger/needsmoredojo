package com.chrisfolger.needsmoredojo.intellij.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jetbrains.annotations.NotNull;

public class MoveRefactoringListener implements RefactoringElementListener
{
    @Override
    public void elementMoved(@NotNull PsiElement psiElement)
    {
        int i=0;
    }

    @Override
    public void elementRenamed(@NotNull PsiElement psiElement) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
