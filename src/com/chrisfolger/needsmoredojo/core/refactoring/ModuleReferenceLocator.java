package com.chrisfolger.needsmoredojo.core.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.DeclareFinder;
import com.chrisfolger.needsmoredojo.core.amd.ImportCreator;
import com.chrisfolger.needsmoredojo.core.amd.SourceLibrary;
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
import java.util.LinkedHashMap;
import java.util.List;

/**
 * this is made to find modules that reference another dojo module
 */
public class ModuleReferenceLocator
{
    protected void getMatch(PsiFile[] files, SourceLibrary[] libraries, DefineStatement statement, String moduleName, PsiFile targetFile)
    {
        // smoke test
        if(!statement.getArguments().getText().contains(moduleName))
        {
            return;
        }

        // get a list of possible modules
        LinkedHashMap<String, PsiFile> results = new ImportCreator().getChoicesFromFiles(files, libraries, moduleName, targetFile, false, true);

        int i=0;
    }

    public PsiFile[] findFilesThatReferenceModule(String moduleName, PsiFile moduleFile, VirtualFile[] projectSourceDirectories)
    {
        List<VirtualFile> directories = new ArrayList<VirtualFile>();
        for(VirtualFile file : projectSourceDirectories)
        {
            directories.add(file);
        }

        Project project = moduleFile.getProject();
        DeclareFinder finder = new DeclareFinder();

        PsiFile[] files = new ImportCreator().getPossibleDojoImportFiles(project, moduleName, true);

        for(PsiFile file : files)
        {
            if(!file.getText().contains("define("))
            {
                continue;
            }

            DefineStatement defineStatement = finder.getDefineStatementItems(file);
            getMatch(files, new ImportCreator().getSourceLibraries(project).toArray(new SourceLibrary[0]), defineStatement, moduleName, file);
        }

        return null;
    }
}
