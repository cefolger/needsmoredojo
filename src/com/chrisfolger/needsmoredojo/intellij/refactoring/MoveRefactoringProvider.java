package com.chrisfolger.needsmoredojo.intellij.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;
import org.jetbrains.annotations.Nullable;

public class MoveRefactoringProvider implements RefactoringElementListenerProvider
{
    @Nullable
    @Override
    public RefactoringElementListener getListener(PsiElement psiElement)
    {
        if(!(psiElement instanceof PsiFile))
        {
            return null;
        }

        PsiFile file = (PsiFile) psiElement;
        String extension = file.getVirtualFile().getExtension();

        if(!extension.equals("js"))
        {
            return null;
        }

        if(!file.getText().contains("define"))
        {
            return null; // not a dojo module
        }

        if(!ServiceManager.getService(file.getProject(), DojoSettings.class).isNeedsMoreDojoEnabled())
        {
            return null; // don't want to refactor if we've disabled Needs More Dojo.
        }

        if(!ServiceManager.getService(file.getProject(), DojoSettings.class).isRefactoringEnabled())
        {
            return null;
        }

        VirtualFile[] sources = SourcesLocator.getProjectSourceDirectories(file.getProject(), true);
        if(sources.length == 0 || sources[0] == null)
        {
            // no project sources, so we can't really refactor
            return null;
        }

        return new MoveRefactoringListener(file, file.getName());
    }
}
