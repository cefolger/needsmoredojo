package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.AMDUtil;
import com.chrisfolger.needsmoredojo.core.util.DefineUtil;
import com.chrisfolger.needsmoredojo.core.util.FileUtil;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.Messages;
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

    public @NotNull String[] getChoicesFromFiles(@NotNull PsiFile[] filesArray, @NotNull SourceLibrary[] libraries, @NotNull String module, @Nullable PsiFile originalModule)
    {
        return getChoicesFromFiles(filesArray, libraries, module, originalModule, false);
    }

    public @NotNull String[] getChoicesFromFiles(@NotNull PsiFile[] filesArray, @NotNull SourceLibrary[] libraries, @NotNull String module, @Nullable PsiFile originalModule, boolean prioritizeRelativePaths)
    {
        List<String> choices = new ArrayList<String>();

        for(int i=0;i<filesArray.length;i++)
        {
            PsiFile file = filesArray[i];

            PsiDirectory directory = file.getContainingDirectory();
            String result = directory.getVirtualFile().getCanonicalPath();

            // parse dojo libraries only
            int firstIndex = Integer.MAX_VALUE;
            SourceLibrary firstLibrary = null;

            for(SourceLibrary library : libraries)
            {
                String fileWithoutLibraryPath = result;
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
                    originalModulePath = firstLibrary.getName() + originalModulePath.substring(originalModulePath.indexOf(firstLibrary.getPath()) + firstLibrary.getPath().length());

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

                if(prioritizeRelativePaths && relativePathOption != null)
                {
                    choices.add(relativePathOption);
                    choices.add(absolutePathOption);
                }
                else
                {
                    choices.add(absolutePathOption);
                    if(relativePathOption != null)
                    {
                        choices.add(relativePathOption);
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

        choices.add(module);
        return choices.toArray(new String[0]);
    }

    public String[] getPossibleDojoImports(PsiFile psiFile, String module, boolean prioritizeRelativeImports)
    {
        PsiFile[] files = null;
        PsiFile[] filesWithUnderscore = null;

        List<SourceLibrary> libraries = new ArrayList<SourceLibrary>();

        try
        {
            VirtualFile dojoSourcesParentDirectory = AMDUtil.getDojoSourcesDirectory(psiFile.getProject(), true);
            if(dojoSourcesParentDirectory != null)
            {
                for(VirtualFile directory : dojoSourcesParentDirectory.getChildren())
                {
                    SourceLibrary library = new SourceLibrary(directory.getName(), directory.getCanonicalPath(), true);
                    libraries.add(library);
                }
            }

            VirtualFile[] otherSourceDirectories = AMDUtil.getProjectSourceDirectories(psiFile.getProject(), true);
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

            files = FilenameIndex.getFilesByName(psiFile.getProject(), module + ".js", GlobalSearchScope.projectScope(psiFile.getProject()));
            // this will let us search for _TemplatedMixin and friends
            filesWithUnderscore = FilenameIndex.getFilesByName(psiFile.getProject(), "_" + module + ".js", GlobalSearchScope.projectScope(psiFile.getProject()));
        }
        catch(NullPointerException exc)
        {
            return new String[] { module };
        }

        Set<PsiFile> allFiles = new HashSet<PsiFile>();
        for(PsiFile file : files) allFiles.add(file);
        for(PsiFile file : filesWithUnderscore) allFiles.add(file);

        PsiFile[] filesArray = allFiles.toArray(new PsiFile[0]);

        return getChoicesFromFiles(filesArray, libraries.toArray(new SourceLibrary[0]), module, psiFile, prioritizeRelativeImports);
    }

    protected void createImport(String module, JSArrayLiteralExpression imports, JSParameterList parameters)
    {
        String parameter = AMDUtil.defineToParameter(module, ServiceManager.getService(parameters.getProject(), DojoSettings.class).getExceptionsMap());

        if(imports.getChildren().length == 0)
        {
            Messages.showInfoMessage("Need at least one import already present", "Add new AMD import");
            return;
        }

        for(JSParameter element : parameters.getParameters())
        {
            if(element.getName().equals(parameter))
            {
                // already defined, so just exit
                new Notification("needsmoredojo", "Add new AMD import", parameter + " is already defined ", NotificationType.INFORMATION).notify(parameters.getProject());
                return;
            }
        }

        JSUtil.addStatementBeforeElement(imports, imports.getChildren()[0], String.format("'%s',", module), "\n");
        JSUtil.addStatementBeforeElement(parameters, parameters.getChildren()[0], parameter + ",", " ");
    }

    public void addImport(PsiFile file, final String module)
    {
        JSRecursiveElementVisitor visitor = new DeclareFinder().getDefineVisitor(new DeclareFinder.CompletionCallback() {
            @Override
            public void run(Object[] result) {
                JSCallExpression callExpression = (JSCallExpression) result[0];
                JSFunction function = (JSFunction) result[1];

                DefineUtil.DefineStatementItems items = new DefineUtil().getDefineStatementItemsFromArguments(callExpression.getArguments());
                createImport(module, items.getArguments(), function.getParameterList());
            }
        });

        file.acceptChildren(visitor);
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
                    initialChoice = expression.getMethodExpression().getText();
                }
            }
        }

        return initialChoice;
    }
}
