package com.chrisfolger.needsmoredojo.core.amd;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

public class SourcesAutoDetector
{
    public PsiFile[] getPossibleSourceRoots(Project project)
    {
        PsiFile[] files = FilenameIndex.getFilesByName(project, "index.html", GlobalSearchScope.projectScope(project));

        return files;
    }
}
