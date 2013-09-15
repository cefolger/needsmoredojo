package com.chrisfolger.needsmoredojo.core.amd.importing;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourceLibrary;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.util.FileUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
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
                    originalModulePath = originalModuleLibrary.getName() + originalModulePath.substring(originalModulePath.indexOf(originalModuleLibrary.getPath()) + originalModuleLibrary.getPath().length());

                    String relativePath = FileUtil.convertToRelativePath(originalModulePath, result);
                    relativePath = NameResolver.convertRelativePathToDojoPath(relativePath);

                    if(relativePath != null)
                    {
                        relativePathOption = relativePath;
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

    public PsiFile[] getPossibleDojoImportFiles(Project project, String module, boolean prioritizeRelativeImports)
    {
        String actualModuleName = NameResolver.getAMDPluginNameIfPossible(module);

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

        Set<PsiFile> allFiles = new HashSet<PsiFile>();
        for(PsiFile file : files) allFiles.add(file);
        for(PsiFile file : filesWithUnderscore) allFiles.add(file);
        for(PsiFile file : filesWithHyphenatedVersion) allFiles.add(file);

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
     * @return a string array of possible modules to import (fully qualified)
     */
    public String[] getPossibleDojoImports(List<SourceLibrary> libraries, PsiFile psiFile, String module, boolean prioritizeRelativeImports)
    {
        PsiFile[] files = getPossibleDojoImportFiles(psiFile.getProject(), module, prioritizeRelativeImports);
        if(files == null)
        {
            return new String[] { module };
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
    public String[] getPossibleDojoImports(PsiFile psiFile, String module, boolean prioritizeRelativeImports)
    {
        return getPossibleDojoImports(new SourcesLocator().getSourceLibraries(psiFile.getProject()), psiFile, module, prioritizeRelativeImports);
    }
}
