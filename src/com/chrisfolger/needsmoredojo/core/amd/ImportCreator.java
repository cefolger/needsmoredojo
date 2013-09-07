package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.*;
import com.intellij.lang.javascript.psi.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ImportCreator
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

    private int getScore(String item)
    {
        int baseScore = 0;

        for(String key : libraryScores.keySet().toArray(new String[0]))
        {
            if(item.indexOf(key) != -1)
            {
                return libraryScores.get(key);
            }
        }

        return 0;
    }

    protected @Nullable SourceLibrary getFirstLibraryThatIncludesFile(@NotNull String fileCanonicalPath, @NotNull SourceLibrary[] libraries)
    {
        int firstIndex = Integer.MAX_VALUE;
        SourceLibrary firstLibrary = null;

        for(SourceLibrary library : libraries)
        {
            String fileWithoutLibraryPath = fileCanonicalPath;
            if(fileWithoutLibraryPath.indexOf(library.getPath()) != -1)
            {
                fileWithoutLibraryPath = library.getName() + fileWithoutLibraryPath.substring(fileWithoutLibraryPath.indexOf(library.getPath()) + library.getPath().length());
            }

            int index = fileWithoutLibraryPath.indexOf(library.getName());
            if(index > -1 && index < firstIndex)
            {
                firstIndex = index;
                firstLibrary = library;
            }
        }

        return firstLibrary;
    }

    public @NotNull String[] getChoicesFromFiles(@NotNull PsiFile[] filesArray, @NotNull SourceLibrary[] libraries, @NotNull String module, @Nullable PsiFile originalModule)
    {
        return getChoicesFromFiles(filesArray, libraries, module, originalModule, false);
    }

    public @NotNull LinkedHashMap<String, PsiFile> getChoicesFromFiles(@NotNull PsiFile[] filesArray, @NotNull SourceLibrary[] libraries, @NotNull String module, @Nullable PsiFile originalModule, boolean prioritizeRelativePaths, boolean getMap)
    {
        Map<String, PsiFile> moduleFileMap = new HashMap<String, PsiFile>();

        List<String> choices = new ArrayList<String>();

        for(int i=0;i<filesArray.length;i++)
        {
            PsiFile file = filesArray[i];

            PsiDirectory directory = file.getContainingDirectory();
            String result = directory.getVirtualFile().getCanonicalPath();

            SourceLibrary firstLibrary = getFirstLibraryThatIncludesFile(result, libraries);

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
                    SourceLibrary originalModuleLibrary = getFirstLibraryThatIncludesFile(originalModulePath, libraries);
                    originalModulePath = originalModuleLibrary.getName() + originalModulePath.substring(originalModulePath.indexOf(originalModuleLibrary.getPath()) + originalModuleLibrary.getPath().length());

                    String relativePath = FileUtil.convertToRelativePath(originalModulePath, result);

                    if(relativePath != null)
                    {
                        // need to use dojo syntax when two files are in the same directory
                        if(relativePath.equals("."))
                        {
                            relativePath = "./";
                        }
                        else if (relativePath.charAt(0) != '.' && relativePath.charAt(0) != '/')
                        {
                            // top level module
                            relativePath = "./" + relativePath;
                        }

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
                return getScore(o2) - getScore(o1);
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
     * Gets a list of dojo packages in the project
     * @param project
     * @return
     */
    public List<SourceLibrary> getSourceLibraries(Project project)
    {
        List<SourceLibrary> libraries = new ArrayList<SourceLibrary>();

        try
        {
            VirtualFile dojoSourcesParentDirectory = SourcesLocator.getDojoSourcesDirectory(project, true);
            if(dojoSourcesParentDirectory != null)
            {
                for(VirtualFile directory : dojoSourcesParentDirectory.getChildren())
                {
                    SourceLibrary library = new SourceLibrary(directory.getName(), directory.getCanonicalPath(), true);
                    libraries.add(library);
                }
            }

            VirtualFile[] otherSourceDirectories = SourcesLocator.getProjectSourceDirectories(project, true);
            for(VirtualFile directory : otherSourceDirectories)
            {
                for(VirtualFile sourceDirectory : directory.getChildren())
                {
                    if(sourceDirectory.getName().contains("."))
                    {
                        continue; // file or hidden directory
                    }

                    SourceLibrary library = new SourceLibrary(sourceDirectory.getName(), sourceDirectory.getCanonicalPath(), true);
                    libraries.add(library);
                }
            }
        }
        catch(NullPointerException exc)
        {
            return libraries;
        }

        Collections.sort(libraries, new Comparator<SourceLibrary>() {
            @Override
            public int compare(SourceLibrary o1, SourceLibrary o2) {
                return o2.getName().length() - o1.getName().length();
            }
        });

        return libraries;
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
        return getPossibleDojoImports(getSourceLibraries(psiFile.getProject()), psiFile, module, prioritizeRelativeImports);
    }

    protected void createImport(String module, JSArrayLiteralExpression imports, JSParameterList parameters)
    {
        String parameter = NameResolver.defineToParameter(module, ServiceManager.getService(parameters.getProject(), DojoSettings.class).getExceptionsMap());

        for(JSParameter element : parameters.getParameters())
        {
            if(element.getName().equals(parameter))
            {
                // already defined, so just exit
                new Notification("needsmoredojo", "Add new AMD import", parameter + " is already defined ", NotificationType.INFORMATION).notify(parameters.getProject());
                return;
            }
        }

        if(imports.getChildren().length == 0)
        {
            // how to insert
            /*
                a few cases to consider:
                define([

                ])

                In my opinion, this is the most readable and the one ImportCreator will account for best:
                define([
                ])

                define([])
             */
            String defineText = imports.getText();
            if(defineText.contains("\n\n"))
            {
                JSUtil.addStatementBeforeElement(imports, imports.getLastChild(), String.format("'%s'", module), "\n");
            }
            else if(defineText.contains("\n"))
            {
                JSUtil.addStatementBeforeElement(imports, imports.getLastChild(), String.format("'%s'", module), "\n");
            }
            else
            {
                JSUtil.addStatementBeforeElement(imports, imports.getLastChild(), String.format("'%s'", module), "");
            }

            JSUtil.addStatementBeforeElement(parameters, parameters.getLastChild(), parameter, "");
        }
        else
        {
            JSUtil.addStatementBeforeElement(imports, imports.getChildren()[0], String.format("'%s',", module), "\n");
            JSUtil.addStatementBeforeElement(parameters, parameters.getChildren()[0], parameter + ",", " ");
        }
    }

    /**
     * entry point for adding an AMD import to an existing define statement
     *
     * @param file the file that the import will be added to
     * @param module the name of the module the user wants to add
     * @return true if the module was added, false otherwise
     */
    public boolean addImport(PsiFile file, final String module)
    {
        final boolean[] visited = {false};

        JSRecursiveElementVisitor visitor = new DeclareFinder().getDefineVisitor(new DeclareFinder.CompletionCallback() {
            @Override
            public void run(Object[] result) {
                JSCallExpression callExpression = (JSCallExpression) result[0];
                JSFunction function = (JSFunction) result[1];

                visited[0] = true;
                DefineStatement items = new DefineUtil().getDefineStatementItemsFromArguments(callExpression.getArguments());
                createImport(module, items.getArguments(), function.getParameterList());
            }
        });

        file.acceptChildren(visitor);
        return visited[0];
    }

    /**
     * when the user adds a new import, this code searches for the nearest possible element
     * to the cursor that they may have wanted to import and returns a suggested choice.
     *
     * I know this method is crude/hard to read and could be way more elegant, however it's good enough for now
     * and produces quite a lot of benefit for low effort
     *
     * TODO this is a good candidate for unit testing...
     */
    public String getSuggestedImport(@Nullable PsiElement element)
    {
        if(element == null)
        {
            return "";
        }

        String initialChoice = "";
        PsiElement parent = element.getParent();
        PsiElement previousSibling = element.getPrevSibling();

        // (underscore represents cursor)
        // we're just over a reference. Example: Site_Util
        if (element.getParent() != null && element.getParent() instanceof JSReferenceExpression)
        {
            initialChoice = element.getText();
        }
        // we're inside a constructor. Example: new Button({_});
        if(element.getParent() instanceof JSObjectLiteralExpression)
        {
            JSObjectLiteralExpression literal = (JSObjectLiteralExpression) element.getParent();
            if(literal.getParent() != null && literal.getParent().getParent() != null && literal.getParent().getParent() instanceof JSNewExpression)
            {
                initialChoice = ((JSNewExpression)literal.getParent().getParent()).getMethodExpression().getText();
            }
        }
        // we're inside a new expression Example: new Button_
        if(parent != null && element.getParent().getParent() != null && parent.getParent() instanceof JSNewExpression)
        {
            initialChoice = ((JSNewExpression)parent.getParent()).getMethodExpression().getText();
        }
        // we're right after a new expression. Example: new Button({}) _
        else if (previousSibling != null && previousSibling.getChildren().length > 0 && previousSibling.getChildren()[0] instanceof JSNewExpression)
        {
            initialChoice = ((JSNewExpression)previousSibling.getChildren()[0]).getMethodExpression().getText();
        }
        // right after a reference. Example: SiteUtil_
        else if (previousSibling != null && previousSibling.getChildren().length > 0 && previousSibling.getChildren()[0] instanceof JSReferenceExpression)
        {
            initialChoice = previousSibling.getChildren()[0].getText();
        }
        // after a variable declaration. Example: var x = new Button({})_
        else if (previousSibling != null && element.getPrevSibling() instanceof JSVarStatement)
        {
            JSVarStatement statement = (JSVarStatement) element.getPrevSibling();
            for(JSVariable variable : statement.getVariables())
            {
                if(variable.getInitializer() instanceof JSNewExpression)
                {
                    JSNewExpression expression = (JSNewExpression) variable.getInitializer();
                    // if these conditions are false, it just means the new expression is not complete
                    if(expression != null && expression.getMethodExpression() != null)
                    {
                        initialChoice = expression.getMethodExpression().getText();
                    }
                }
            }
        }

        return initialChoice;
    }
}
