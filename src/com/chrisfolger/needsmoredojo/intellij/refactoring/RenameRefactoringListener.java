package com.chrisfolger.needsmoredojo.intellij.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourceLibrary;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportResolver;
import com.chrisfolger.needsmoredojo.core.refactoring.ModuleImporter;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jetbrains.annotations.NotNull;

/**
 *  provides a rename operation for dojo modules that will rename the define literal and parameters,
 *  while using the correct path syntax.
 */
public class RenameRefactoringListener implements RefactoringElementListener {
    private String originalFile = null;
    private PsiFile[] possibleFiles = new PsiFile[0];
    private PsiFile originalPsiFile;
    private String originalPath;

    public RenameRefactoringListener(PsiFile originalPsiFile, String originalFile)
    {
        this.originalFile = originalFile;
        this.originalPsiFile = originalPsiFile;
        SourceLibrary[] libraries = new SourcesLocator().getSourceLibraries(originalPsiFile.getProject()).toArray(new SourceLibrary[0]);
        final SourceLibrary libraryFileIsIn = new SourcesLocator().getFirstLibraryThatIncludesFile(originalPsiFile.getVirtualFile().getCanonicalPath(), libraries);
        this.originalPath = ImportResolver.getAbsolutePathRelativeToLibrary(libraryFileIsIn, originalPsiFile.getContainingDirectory().getVirtualFile().getCanonicalPath(), originalPsiFile);

        possibleFiles = new ImportResolver().getPossibleDojoImportFiles(originalPsiFile.getProject(), originalFile.substring(0, originalFile.indexOf('.')), true, false);
    }

    @Override
    public void elementMoved(@NotNull PsiElement psiElement) {
    }

    @Override
    public void elementRenamed(@NotNull final PsiElement psiElement)
    {
        final String moduleName = originalFile.substring(0, originalFile.indexOf('.'));
        SourceLibrary[] libraries = new SourcesLocator().getSourceLibraries(psiElement.getProject()).toArray(new SourceLibrary[0]);
        final SourceLibrary libraryFileIsIn = new SourcesLocator().getFirstLibraryThatIncludesFile(originalPsiFile.getVirtualFile().getCanonicalPath(), libraries);

        final ModuleImporter moduleImporter = new ModuleImporter(possibleFiles,
                moduleName,
                (PsiFile) psiElement,
                new SourcesLocator().getSourceLibraries(psiElement.getProject()).toArray(new SourceLibrary[0]),
                ServiceManager.getService(psiElement.getProject(),
                        DojoSettings.class).getNamingExceptionList(),
                originalPath);

        CommandProcessor.getInstance().executeCommand(psiElement.getProject(), new Runnable() {
            @Override
            public void run() {
                moduleImporter.findFilesThatReferenceModule(SourcesLocator.getProjectSourceDirectories(psiElement.getProject(), true), true);
            }
        },
        "Rename Dojo Module",
        "Rename Dojo Module");

        moduleImporter.reimportModuleId((PsiFile) psiElement);
    }
}
