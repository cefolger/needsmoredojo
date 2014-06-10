package com.chrisfolger.needsmoredojo.core.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourceLibrary;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportResolver;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportUpdater;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameException;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.util.FileUtil;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * this is made to find modules that reference another dojo module
 */
public class ModuleImporter
{
    private PsiFile[] possibleFiles;
    private SourceLibrary[] libraries;
    private PsiFile moduleFile;
    private Project project;
    private String moduleName;
    private List<NameException> moduleNamingExceptionMap;
    private ImportUpdater importUpdater;

    public ModuleImporter(PsiFile[] possibleImportFiles, String moduleName, PsiFile moduleFile, SourceLibrary[] libraries, List<NameException> exceptionsMap)
    {
        this.moduleName = moduleName;
        this.moduleFile = moduleFile;
        this.project = moduleFile.getProject();
        this.libraries = libraries;
        this.possibleFiles = possibleImportFiles;
        this.moduleNamingExceptionMap = exceptionsMap;
        importUpdater = new ImportUpdater(exceptionsMap);
    }

    /**
     * Determines if one module references another module, and returns a result if it does
     *
     * @param newModuleName the referenced module's name
     * @param statement the module's parsed define statement
     * @param targetFile the module file
     * @return
     */
    protected @Nullable MatchResult getMatch(@NotNull String newModuleName, @NotNull DefineStatement statement, @NotNull PsiFile targetFile)
    {
        // smoke test
        if(!statement.getArguments().getText().contains(moduleName))
        {
            return null;
        }

        // get a list of possible modules and their syntax
        LinkedHashMap<String, PsiFile> results = new ImportResolver().getChoicesFromFiles(possibleFiles, libraries, moduleName, targetFile, false, true);

        // go through the defines and determine if there is a match
        int matchIndex = -1;
        String matchedString = "";
        String matchedPostfix = "";
        char quote = '\'';

        for(int i=0;i<statement.getArguments().getExpressions().length;i++)
        {
            JSExpression argument = statement.getArguments().getExpressions()[i];

            String rawArgumentText = argument.getText();
            if(rawArgumentText.contains("\""))
            {
                quote = '"';
            }

            String argumentText = rawArgumentText.replaceAll("'", "").replaceAll("\"", "");
            if(argumentText.contains(moduleName))
            {
                StringBuilder b = new StringBuilder(argumentText);
                b.replace(argumentText.lastIndexOf(moduleName), argumentText.lastIndexOf(moduleName) + moduleName.length(), newModuleName );
                argumentText = b.toString();
            }

            // in case it's a plugin
            // TODO unit test please !
            String modulePath = NameResolver.getModulePath(argumentText);
            String finalArgumentText = modulePath + NameResolver.getModuleName(argumentText);
            String pluginPostFix = NameResolver.getAMDPluginResourceIfPossible(argumentText, true);

            if(results.containsKey(finalArgumentText))
            {
                matchIndex = i;
                matchedString = finalArgumentText;
                matchedPostfix = pluginPostFix;
                break;
            }
        }

        if(matchIndex == -1)
        {
            return null;
        }

        return new MatchResult(targetFile, matchIndex, matchedString, quote, matchedPostfix, null, statement.getCallExpression());
    }



    private String chooseImportToReplaceAnImport(String original, String[] choices)
    {
        String chosenImport = choices[0];

        // if more than one syntax is possible, use the one that matches whatever the old import matched.
        if(choices.length > 1)
        {
            if(original.contains("./") && !choices[0].contains("./"))
            {
                // use relative path option
                chosenImport = choices[1];
            }
            else if (!(original.contains("./")) && choices[0].contains("./"))
            {
                // use absolute path option
                chosenImport = choices[1];
            }
        }

        return chosenImport;
    }

    private String getUpdatedPluginResource(PsiFile pluginResourceFile, String pluginPostfix, PsiFile currentModule)
    {
        if(pluginResourceFile != null)
        {
            String relativePath = NameResolver.convertRelativePathToDojoPath(FileUtil.convertToRelativePath(currentModule.getContainingDirectory().getVirtualFile().getCanonicalPath(), pluginResourceFile.getVirtualFile().getCanonicalPath()));
            if(relativePath != null)
            {
                return "!" + relativePath;
            }
        }
        else if (pluginPostfix != null && pluginPostfix.length() > 1)
        {
            // don't do anything for the absolute path case, because it would not have changed.
        }

        return pluginPostfix;
    }

    /**
     * takes an existing AMD import and updates it. Used when a module changes locations
     *
     * @param index the index of the import
     * @param currentModule the module that is being updated
     * @param quote the quote symbol to use for imports
     * @param path the new module's path
     * @param newModule the new module
     */
    public void reimportModule(int index, PsiFile currentModule, char quote, String path, PsiFile newModule, String pluginPostfix, boolean updateReferences, PsiFile pluginResourceFile, JSCallExpression callExpression)
    {
        DefineStatement defineStatement = new DefineResolver().getDefineStatementItemsFromArguments(callExpression.getArguments(), callExpression);

        String newModuleName = newModule.getName().substring(0, newModule.getName().indexOf('.'));
        LinkedHashMap<String, PsiFile> results = new ImportResolver().getChoicesFromFiles(new PsiFile[] { newModule }, libraries, newModuleName, currentModule, false, true);

        // check if the original used a relative syntax or absolute syntax, and prefer that
        String[] possibleImports = results.keySet().toArray(new String[0]);
        String chosenImport = chooseImportToReplaceAnImport(path, possibleImports);

        pluginPostfix = getUpdatedPluginResource(pluginResourceFile, pluginPostfix, currentModule);

        PsiElement defineLiteral = defineStatement.getArguments().getExpressions()[index];
        PsiElement newImport = JSUtil.createExpression(defineLiteral.getParent(), quote + chosenImport + pluginPostfix + quote);

        MatchResult match = new MatchResult(currentModule, index, path, quote, pluginPostfix, pluginResourceFile, callExpression);
        importUpdater.updateModuleReference(currentModule, match, defineStatement, newImport, updateReferences);
    }

    /**
     * during a move, modules that import the moved module need to have their references updated
     * This method takes care of that
     *
     * @param matchResult This is the match object that has the index of the define literal to update
     * @param newModule this is the new module to reimport
     */
    public void reimportModule(MatchResult matchResult, PsiFile newModule)
    {
        reimportModule(matchResult.getIndex(), matchResult.getModule(), matchResult.getQuote(), matchResult.getPath(), newModule, matchResult.getPluginResourceId(), true, matchResult.getPluginResourceFile(), matchResult.getCallExpression());
    }

    /**
     * given a module, returns a list of modules that it references in the define block
     *
     * @param module
     * @return
     */
    public List<MatchResult> findFilesThatModuleReferences(PsiFile module)
    {
        DefineResolver resolver = new DefineResolver();
        Set<JSCallExpression> callExpressions = resolver.getAllImportBlocks(module);
        List<MatchResult> matches = new ArrayList<MatchResult>();

        for(JSCallExpression callExpression : callExpressions)
        {
            if(callExpression == null)
            {
                continue;
            }

            DefineStatement statement = resolver.getDefineStatementItemsFromArguments(callExpression.getArguments(), callExpression);
            if(statement == null)
            {
                // define statement wasn't valid
                return matches;
            }

            for(int i=0;i<statement.getArguments().getExpressions().length;i++)
            {
                JSExpression expression = statement.getArguments().getExpressions()[i];

                char quote = '"';
                String expressionText = expression.getText();
                if(expressionText.contains("'"))
                {
                    quote = '\'';
                }

                // figure out which module it is
                String importModule = expressionText.replaceAll("'", "").replaceAll("\"", "");

                // get the module name
                String moduleName = NameResolver.getModuleName(importModule);
                String pluginResourceId = NameResolver.getAMDPluginResourceIfPossible(importModule, true);
                String modulePath = NameResolver.getModulePath(importModule);

                PsiFile pluginResourceFile = null;
                if(pluginResourceId.startsWith("!") && pluginResourceId.length() > 2 && pluginResourceId.charAt(1) == '.')
                {
                    PsiReference[] fileReferences = expression.getReferences();

                    if(fileReferences.length > 0)
                    {
                        PsiReference potentialReference = fileReferences[fileReferences.length-1];
                        pluginResourceFile = (PsiFile) potentialReference.resolve();
                    }
                }
                else if (pluginResourceId.startsWith("!") && pluginResourceId.length() > 2)
                {
                    // this is an absolute reference
                }

                // get the list of possible strings/PsiFiles that would match it
                PsiFile[] files = new ImportResolver().getPossibleDojoImportFiles(module.getProject(), moduleName, true, false);

                // get the files that are being imported
                // TODO performance optimization
                LinkedHashMap<String, PsiFile> results = new ImportResolver().getChoicesFromFiles(files, libraries, moduleName, module.getContainingFile(), false, true);
                if(results.containsKey(modulePath + moduleName))
                {
                    MatchResult match = new MatchResult(results.get(modulePath + moduleName), i, modulePath + moduleName, quote, pluginResourceId, pluginResourceFile, callExpression);
                    matches.add(match);
                }
            }
        }

        return matches;
    }

    /**
     * entry point for renaming a dojo module
     *
     * @param projectSourceDirectories This is the list of directories to search for modules in
     * @return A list of results that represent modules that reference the renamed module
     */
    public @Nullable List<MatchResult> findFilesThatReferenceModule(@NotNull VirtualFile[] projectSourceDirectories, boolean update)
    {
        List<MatchResult> matches = new ArrayList<MatchResult>();

        List<VirtualFile> directories = new ArrayList<VirtualFile>();
        for(VirtualFile file : projectSourceDirectories)
        {
            directories.add(file);
        }

        DefineResolver resolver = new DefineResolver();
        PsiManager psiManager = PsiManager.getInstance(project);

        // TODO can we use a directory scope instead???
        Collection<VirtualFile> results = FilenameIndex.getAllFilesByExt(project, "js", GlobalSearchScope.projectScope(project));

        for(VirtualFile directory : directories)
        {
            for(VirtualFile file : results)
            {
                // TODO ignore dojo files for this release
                // TODO intelligently decide whether to use relative or absolute paths
                if(DojoModuleFileResolver.isInDojoSources(file.getCanonicalPath()))
                {
                    continue;
                }

                boolean isInProjectDirectory = VfsUtil.isAncestor(directory, file, true);
                if(!isInProjectDirectory) continue;

                PsiFile psiFile = psiManager.findFile(file);
                String psiFileText = psiFile.getText();
                if(!psiFileText.contains("define(") && !psiFileText.contains("require"))
                {
                    continue;
                }

                if(!psiFileText.contains(moduleName))
                {
                    continue;
                }

                Set<JSCallExpression> importBlocks = resolver.getAllImportBlocks(psiFile);
                for(JSCallExpression expression : importBlocks)
                {
                    DefineStatement defineStatement = resolver.getDefineStatementItemsFromArguments(expression.getArguments(), expression);

                    // possible that the file passed the smoke test but is not a real module
                    if(defineStatement == null)
                    {
                        continue;
                    }

                    MatchResult match = getMatch(moduleFile.getName().substring(0, moduleFile.getName().indexOf('.')), defineStatement, psiFile);

                    if(match != null)
                    {
                        matches.add(match);

                        if(update)
                        {
                            importUpdater.updateModuleReference(psiFile, match, defineStatement, true);
                        }
                    }
                }
            }
        }

        return matches;
    }
}
