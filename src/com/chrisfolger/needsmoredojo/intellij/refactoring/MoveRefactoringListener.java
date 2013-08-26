package com.chrisfolger.needsmoredojo.intellij.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.ImportCreator;
import com.chrisfolger.needsmoredojo.core.amd.SourceLibrary;
import com.chrisfolger.needsmoredojo.core.refactoring.ModuleRenamer;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.AMDUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MoveRefactoringListener implements RefactoringElementListener
{
    private String originalFile = null;
    private PsiFile[] possibleFiles = new PsiFile[0];
    private List<ModuleRenamer.MatchResult> matches = new ArrayList<ModuleRenamer.MatchResult>();
    private ModuleRenamer renamer = null;

    public MoveRefactoringListener(PsiFile originalPsiFile, String originalFile)
    {
        this.originalFile = originalFile;
        possibleFiles = new ImportCreator().getPossibleDojoImportFiles(originalPsiFile.getProject(), originalFile.substring(0, originalFile.indexOf('.')), true);

        renamer = new ModuleRenamer(possibleFiles,
                originalFile.substring(0, originalFile.indexOf('.')),
                originalPsiFile,
                new ImportCreator().getSourceLibraries(originalPsiFile.getProject()).toArray(new SourceLibrary[0]),
                ServiceManager.getService(originalPsiFile.getProject(),
                        DojoSettings.class).getExceptionsMap());


        renamer.findFilesThatModuleReferences(originalPsiFile);

        // here is where we need to go through, find all of the modules that reference this module, and produce a list of MatchResults
        matches = renamer.findFilesThatReferenceModule(AMDUtil.getProjectSourceDirectories(originalPsiFile.getProject(), true), false);
    }

    /**
     * in this method, we've already identified which modules we need to update references in.
     * So, we are just going to go through each one and re-import the module using its new path and name
     *
     * @param psiElement
     */
    @Override
    public void elementMoved(@NotNull PsiElement psiElement)
    {
        PsiFile file = (PsiFile) psiElement;

        for(ModuleRenamer.MatchResult result : matches)
        {
            renamer.reimportModule(result, file);
        }
    }

    @Override
    public void elementRenamed(@NotNull PsiElement psiElement) {
    }
}
