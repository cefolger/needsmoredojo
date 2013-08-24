package com.chrisfolger.needsmoredojo.core.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.DeclareFinder;
import com.chrisfolger.needsmoredojo.core.util.DefineStatement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * this is made to find modules that reference another dojo module
 */
public class ModuleReferenceLocator
{
    public PsiFile[] findFilesThatReferenceModule(String moduleName, PsiFile moduleFile, VirtualFile[] projectSourceDirectories)
    {
        List<VirtualFile> directories = new ArrayList<VirtualFile>();
        for(VirtualFile file : projectSourceDirectories)
        {
            directories.add(file);
        }

        Project project = moduleFile.getProject();
        PsiManager psiManager = PsiManager.getInstance(project);
        DeclareFinder finder = new DeclareFinder();
        // TODO can we use a directory scope instead???
        Collection<VirtualFile> results = FilenameIndex.getAllFilesByExt(project, "js", GlobalSearchScope.projectScope(project));

        for(VirtualFile file : results)
        {
            boolean isInProjectDirectory = VfsUtil.isAncestor(projectSourceDirectories[0], results.toArray(new VirtualFile[0])[0], true);
            if(!isInProjectDirectory) continue;

            PsiFile psiFile = psiManager.findFile(file);
            if(!psiFile.getText().contains("define("))
            {
                continue;
            }

            DefineStatement defineStatement = finder.getDefineStatementItems(psiFile);
        }

        return null;
    }
}
