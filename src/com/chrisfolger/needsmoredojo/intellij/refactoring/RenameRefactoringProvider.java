package com.chrisfolger.needsmoredojo.intellij.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;
import org.jetbrains.annotations.Nullable;

public class RenameRefactoringProvider implements RefactoringElementListenerProvider {
    @Nullable
    @Override
    public RefactoringElementListener getListener(PsiElement psiElement) {
        if(!(psiElement instanceof  PsiFile))
        {
            return null;
        }

        PsiFile file = (PsiFile) psiElement;
        String extension = file.getVirtualFile().getExtension();

        if(!extension.equals("js"))
        {
            return null;
        }

        return new RenameRefactoringListener(file, file.getName());
    }
}
