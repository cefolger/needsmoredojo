package com.chrisfolger.needsmoredojo.core.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.DeclareFinder;
import com.chrisfolger.needsmoredojo.core.amd.ImportCreator;
import com.chrisfolger.needsmoredojo.core.amd.SourceLibrary;
import com.chrisfolger.needsmoredojo.core.util.AMDUtil;
import com.chrisfolger.needsmoredojo.core.util.DefineStatement;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.RefactoringFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * this is made to find modules that reference another dojo module
 */
public class ModuleRenamer
{
    private PsiFile[] possibleFiles;
    private SourceLibrary[] libraries;
    private PsiFile moduleFile;
    private Project project;
    private String moduleName;

    private class MatchResult
    {
        private int index;
        private String path;
        private char quote;

        private MatchResult(int index, String path, char quote) {
            this.index = index;
            this.path = path;
            this.quote = quote;
        }

        private char getQuote() {
            return quote;
        }

        private int getIndex() {
            return index;
        }

        private String getPath() {
            return path;
        }
    }

    public ModuleRenamer(PsiFile[] possibleImportFiles, String moduleName, PsiFile moduleFile, SourceLibrary[] libraries)
    {
        this.moduleName = moduleName;
        this.moduleFile = moduleFile;
        this.project = moduleFile.getProject();
        this.libraries = libraries;
        this.possibleFiles = possibleImportFiles;
    }

    protected @Nullable MatchResult getMatch(@NotNull String newModuleName, @NotNull DefineStatement statement, @NotNull PsiFile targetFile)
    {
        // smoke test
        if(!statement.getArguments().getText().contains(moduleName))
        {
            return null;
        }

        // get a list of possible modules and their syntax
        LinkedHashMap<String, PsiFile> results = new ImportCreator().getChoicesFromFiles(possibleFiles, libraries, moduleName, targetFile, false, true);

        // go through the defines and determine if there is a match
        int matchIndex = -1;
        String matchedString = "";
        char quote = '\'';

        for(int i=0;i<statement.getArguments().getExpressions().length;i++)
        {
            JSExpression argument = statement.getArguments().getExpressions()[i];

            if(argument.getText().contains("\""))
            {
                quote = '"';
            }

            String argumentText = argument.getText().replaceAll("'", "").replace("\"", "");
            if(argumentText.contains(moduleName))
            {
                StringBuilder b = new StringBuilder(argumentText);
                b.replace(argumentText.lastIndexOf(moduleName), argumentText.lastIndexOf(moduleName) + moduleName.length(), newModuleName );
                argumentText = b.toString();
            }

            if(results.containsKey(argumentText))
            {
                matchIndex = i;
                matchedString = argumentText;
                break;
            }
        }

        return new MatchResult(matchIndex, matchedString, quote);
    }

    // TODO cleanup and doc
    // TODO look into undo operations
    public void updateModuleReference(final PsiFile targetFile, final MatchResult match, final DefineStatement statement)
    {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                PsiElement defineLiteral = statement.getArguments().getExpressions()[match.getIndex()];
                defineLiteral.replace(JSUtil.createExpression(defineLiteral.getParent(), match.getQuote() + match.getPath() + match.getQuote()));

                // TODO pull exceptions map for define to parameter conversion
                RefactoringFactory.getInstance(targetFile.getProject())
                        .createRename(statement.getFunction().getParameters()[match.getIndex()], AMDUtil.defineToParameter(match.getPath(), new HashMap<String, String>()), false, false)
                        .run();
            }
        });
    }

    public @Nullable PsiFile[] findFilesThatReferenceModule(@NotNull VirtualFile[] projectSourceDirectories)
    {
        List<VirtualFile> directories = new ArrayList<VirtualFile>();
        for(VirtualFile file : projectSourceDirectories)
        {
            directories.add(file);
        }

        DeclareFinder finder = new DeclareFinder();
        PsiManager psiManager = PsiManager.getInstance(project);

        // TODO can we use a directory scope instead???
        Collection<VirtualFile> results = FilenameIndex.getAllFilesByExt(project, "js", GlobalSearchScope.projectScope(project));

        for(VirtualFile file : results)
        {
            boolean isInProjectDirectory = VfsUtil.isAncestor(projectSourceDirectories[0], file, true);
            if(!isInProjectDirectory) continue;

            PsiFile psiFile = psiManager.findFile(file);
            if(!psiFile.getText().contains("define("))
            {
                continue;
            }

            DefineStatement defineStatement = finder.getDefineStatementItems(psiFile);
            MatchResult match = getMatch(moduleFile.getName().substring(0, moduleFile.getName().indexOf('.')), defineStatement, psiFile);

            if(match != null)
            {
                updateModuleReference(psiFile, match, defineStatement);
            }
        }
        return null;
    }
}
