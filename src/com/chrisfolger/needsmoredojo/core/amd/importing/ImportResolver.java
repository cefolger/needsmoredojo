package com.chrisfolger.needsmoredojo.core.amd.importing;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourceLibrary;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.util.FileUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ImportResolver
{
    private static final Map<String, Integer> libraryScores = new HashMap<String, Integer>();

    static
    {
        libraryScores.put("dojo/tests", 0);
        libraryScores.put("dojo/", 5);
        libraryScores.put("dijit/", 4);
        libraryScores.put("dgrid/", 2);
        libraryScores.put("dojox/", 1);
        libraryScores.put("doh/", 1);
        libraryScores.put("build/", 1);
    }

    private int getScore(String item, String secondItem)
    {
        int baseScore = 0;

        // one non-nls module vs another one should be scored lower
        if(item.contains("nls") && !secondItem.contains("nls"))
        {
            baseScore -= 1;
        }
        else if (!item.contains("nls") && secondItem.contains("nls"))
        {
            baseScore += 1;
        }

        for(String key : libraryScores.keySet().toArray(new String[0]))
        {
            if(item.indexOf(key) != -1)
            {
                return libraryScores.get(key) + baseScore;
            }
        }

        return 0;
    }

    public @NotNull String[] getChoicesFromFiles(@NotNull PsiFile[] filesArray, @NotNull SourceLibrary[] libraries, @NotNull String module, @Nullable PsiFile originalModule)
    {
        return getChoicesFromFiles(filesArray, libraries, module, originalModule, false);
    }

    public @NotNull LinkedHashMap<String, PsiFile> getChoicesFromFiles(@NotNull PsiFile[] filesArray, @NotNull SourceLibrary[] libraries, @NotNull String module, @Nullable PsiFile originalModule, boolean prioritizeRelativePaths, boolean getMap)
    {
        Map<String, PsiFile> moduleFileMap = new HashMap<String, PsiFile>();

        SourcesLocator locator = new SourcesLocator();
        List<String> choices = new ArrayList<String>();

        for(int i=0;i<filesArray.length;i++)
        {
            PsiFile file = filesArray[i];

            PsiDirectory directory = file.getContainingDirectory();
            String result = directory.getVirtualFile().getCanonicalPath();

            SourceLibrary firstLibrary = locator.getFirstLibraryThatIncludesFile(result, libraries);

            if(firstLibrary != null)
            {
                if(!firstLibrary.getPath().equals(""))
                {
                    result = firstLibrary.getName() + result.substring(result.indexOf(firstLibrary.getPath()) + firstLibrary.getPath().length());
                }
                result = result.substring(result.indexOf(firstLibrary.getName()));
                result = result.replace('\\', '/') + '/' + file.getName().substring(0, file.getName().indexOf('.'));

                String originalModulePath = null;
                String relativePathOption = null;
                String absolutePathOption = null;

                if(originalModule != null)
                {
                    originalModulePath = originalModule.getContainingDirectory().getVirtualFile().getCanonicalPath();
                    SourceLibrary originalModuleLibrary = locator.getFirstLibraryThatIncludesFile(originalModulePath, libraries);

                    try {
                        originalModulePath = originalModuleLibrary.getName() + originalModulePath.substring(originalModulePath.indexOf(originalModuleLibrary.getPath()) + originalModuleLibrary.getPath().length());

                        String relativePath = FileUtil.convertToRelativePath(originalModulePath, result);
                        relativePath = NameResolver.convertRelativePathToDojoPath(relativePath);

                        if(relativePath != null)
                        {
                            relativePathOption = relativePath;
                        }
                    }
                    catch(IndexOutOfBoundsException exc)
                    {
                        // for this case, it's not fatal, it just means we can't use relative paths.
                        Logger.getLogger(ImportResolver.class).info(originalModulePath + " could not be used in a relative path import");
                    }
                    catch(NullPointerException exc)
                    {
                        // for this case, it's not fatal, it just means we can't use relative paths.
                        Logger.getLogger(ImportResolver.class).info(originalModulePath + " could not be used in a relative path import");
                    }
                }

                absolutePathOption = result;
                String pluginPostFix = NameResolver.getAMDPluginResourceIfPossible(module, true);

                if(prioritizeRelativePaths && relativePathOption != null)
                {
                    choices.add(relativePathOption + pluginPostFix);
                    moduleFileMap.put(relativePathOption + pluginPostFix, file);
                    choices.add(absolutePathOption + pluginPostFix);
                    moduleFileMap.put(absolutePathOption + pluginPostFix, file);
                }
                else
                {
                    choices.add(absolutePathOption + pluginPostFix);
                    moduleFileMap.put(absolutePathOption + pluginPostFix, file);

                    if(relativePathOption != null)
                    {
                        choices.add(relativePathOption + pluginPostFix);
                        moduleFileMap.put(relativePathOption + pluginPostFix, file);
                    }
                }
            }
        }

        Collections.sort(choices, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return getScore(o2, o1) - getScore(o1, o2);
            }
        });

        // sort the map based on score.
        LinkedHashMap<String, PsiFile> finalMapResults = new LinkedHashMap<String, PsiFile>();
        for(String choice : choices)
        {
            for(Map.Entry<String, PsiFile> entry : moduleFileMap.entrySet())
            {
                if(choice.equals(entry.getKey()))
                {
                    finalMapResults.put(entry.getKey(), entry.getValue());
                    break;
                }
            }
        }

        choices.add(module);
        return finalMapResults;
    }


    /**
     * given a list of dojo modules to search through and a list of libraries,
     * returns a list of possible dojo modules
     *
     * @param filesArray the array of dojo modules to search through
     * @param libraries a list of source libraries that contain dojo modules
     * @param module the original module name that the user entered
     * @param originalModule the file that the user started entering this module in
     * @param prioritizeRelativePaths if true, return relative path syntax first before absolute path syntax
     * @return a string array of dojo modules that the user may have been searching for
     */
    public @NotNull String[] getChoicesFromFiles(@NotNull PsiFile[] filesArray, @NotNull SourceLibrary[] libraries, @NotNull String module, @Nullable PsiFile originalModule, boolean prioritizeRelativePaths)
    {
        return getChoicesFromFiles(filesArray, libraries, module, originalModule, prioritizeRelativePaths, true).keySet().toArray(new String[0]);
    }

    /**
     * gets a list of all files that match a module name
     *
     * @param module    the module to search on
     * @param prioritizeRelativeImports if true, will return relative path modules first instead of absolutely referenced files
     * @param allowCaseInsensitiveSearch if true, you can type "contentpane" and get "ContentPane" and similar things.
     * @return an array of possible files that match the given module name
     */
    public PsiFile[] getPossibleDojoImportFiles(Project project, String module, boolean prioritizeRelativeImports, boolean allowCaseInsensitiveSearch)
    {
        String actualModuleName = NameResolver.getAMDPluginNameIfPossible(module);
        Set<PsiFile> allFiles = new HashSet<PsiFile>();

        // I decided to allow both case-insensitive and case-sensitive searches at the moment. This is because I did some
        // crude profiling and here's what I got when a project contained all of the dojo sources + the project sources:

        // case-sensitive search, index on SSD, project on SSD: 1ms
        // case-insensitive search, index on SSD, project on SSD: 100ms
        // case-insensitive search, index on SSD, project on HDD: 100ms
        // case-insensitive search, index on HDD, project on HDD: 500ms

        // It doesn't really matter that it takes half a second though, so I will most likely remove the
        // case-insensitive option entirely in future releases. It also took 100ms after the first import due to
        // caching I assume. 
        if(!allowCaseInsensitiveSearch)
        {
            PsiFile[] files = null;
            PsiFile[] filesWithUnderscore = null;
            PsiFile[] filesWithHyphenatedVersion = new PsiFile[0];

            try
            {
                files = FilenameIndex.getFilesByName(project, actualModuleName + ".js", GlobalSearchScope.projectScope(project));
                // this will let us search for _TemplatedMixin and friends
                filesWithUnderscore = FilenameIndex.getFilesByName(project, "_" + actualModuleName + ".js", GlobalSearchScope.projectScope(project));
                // search for dom-attr and friends when you have typed domAttr
                String hyphenatedModule = NameResolver.getPossibleHyphenatedModule(module);
                if(hyphenatedModule != null)
                {
                    filesWithHyphenatedVersion = FilenameIndex.getFilesByName(project, hyphenatedModule + ".js", GlobalSearchScope.projectScope(project));
                }
            }
            catch(NullPointerException exc)
            {
                return null;
            }

            for(PsiFile file : files) allFiles.add(file);
            for(PsiFile file : filesWithUnderscore) allFiles.add(file);
            for(PsiFile file : filesWithHyphenatedVersion) allFiles.add(file);
        }
        else
        {
            Collection<VirtualFile> results = FilenameIndex.getAllFilesByExt(project, "js", GlobalSearchScope.projectScope(project));

            String baseFilename = actualModuleName + ".js";
            String underscoreName = "_" + actualModuleName + ".js";

            for(VirtualFile file : results)
            {
                if(file.getName().equalsIgnoreCase(baseFilename) || file.getName().equalsIgnoreCase(underscoreName))
                {
                    allFiles.add(PsiManager.getInstance(project).findFile(file));
                }
            }
        }

        PsiFile[] filesArray = allFiles.toArray(new PsiFile[0]);
        return filesArray;
    }

    /**
     * gets a list of possible modules to import based on source files and a user entered module
     *
     * @param libraries the list of libraries to search for modules in
     * @param psiFile   the current file the user is adding an import to
     * @param module    the module the user wanted to add
     * @param prioritizeRelativeImports if true, will return relative path modules first instead of absolutely referenced files
     * @param useEnteredModuleAsChoice if true, the entered module name will be returned as a possible import.
     * @param allowCaseInsensitiveSearch if true, you can type "contentpane" and get "ContentPane" and similar things.
     * @return a string array of possible modules to import (fully qualified)
     */
    public String[] getPossibleDojoImports(List<SourceLibrary> libraries, PsiFile psiFile, String module, boolean prioritizeRelativeImports, boolean useEnteredModuleAsChoice, boolean allowCaseInsensitiveSearch)
    {
        PsiFile[] files = getPossibleDojoImportFiles(psiFile.getProject(), module, prioritizeRelativeImports, allowCaseInsensitiveSearch);
        if((files == null || files.length == 0) && useEnteredModuleAsChoice)
        {
            return new String[] { module };
        }
        else if (files == null || files.length == 0)
        {
            return new String[0];
        }

        return getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), module, psiFile, prioritizeRelativeImports);
    }

    /**
     * gets a list of possible modules to import based on source files and a user entered module
     *
     * @param psiFile   the current file the user is adding an import to
     * @param module    the module the user wanted to add
     * @param prioritizeRelativeImports if true, will return relative path modules first instead of absolutely referenced files
     * @return a string array of possible modules to import (fully qualified)
     */
    public String[] getPossibleDojoImports(PsiFile psiFile, String module, boolean prioritizeRelativeImports, boolean useEnteredModuleAsChoice, boolean allowCaseInsensitiveSearch)
    {
        return getPossibleDojoImports(new SourcesLocator().getSourceLibraries(psiFile.getProject()), psiFile, module, prioritizeRelativeImports, useEnteredModuleAsChoice, allowCaseInsensitiveSearch);
    }
}
