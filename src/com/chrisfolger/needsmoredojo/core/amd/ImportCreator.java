package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.AMDUtil;
import com.chrisfolger.needsmoredojo.core.util.DefineUtil;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ImportCreator
{
    private int getScore(String item)
    {
        Map<String, Integer> scores = new LinkedHashMap<String, Integer>();
        scores.put("dojo/tests", 0);
        scores.put("dojo/", 5);
        scores.put("dijit/", 4);
        scores.put("dgrid/", 2);
        scores.put("dojox/", 1);

        for(String key : scores.keySet().toArray(new String[0]))
        {
            if(item.indexOf(key) != -1)
            {
                return scores.get(key);
            }
        }

        return 0;
    }

    // go through each source directory
    // if source is substring of file then file is part of that module, so substring it



    public @NotNull String[] getChoicesFromFiles(@NotNull PsiFile[] filesArray, @NotNull SourceLibrary[] libraries, @NotNull String module)
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
                choices.add(result);
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

    public String[] getPossibleDojoImports(PsiFile psiFile, String module)
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
                    SourceLibrary library = new SourceLibrary(directory.getName(), directory.getCanonicalPath());
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

                    SourceLibrary library = new SourceLibrary(sourceDirectory.getName(), sourceDirectory.getCanonicalPath());
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

        return getChoicesFromFiles(filesArray, libraries.toArray(new SourceLibrary[0]), module);
    }

    protected void createImport(String module, JSArrayLiteralExpression imports, JSParameterList parameters)
    {
        if(imports.getChildren().length == 0)
        {
            Messages.showInfoMessage("Need at least one import already present", "Add new AMD import");
            return;
        }
        else
        {
            JSUtil.addStatementBeforeElement(imports, imports.getChildren()[0], String.format("'%s',", module), "\n");
            JSUtil.addStatementBeforeElement(parameters, parameters.getChildren()[0], AMDUtil.defineToParameter(module, ServiceManager.getService(parameters.getProject(), DojoSettings.class).getExceptionsMap()) + ",", " ");
        }
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
}
