package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.*;

public class ImportCreator
{
    public static final String[] dojoLibraries = new String[] { "dojo", "dijit", "dojox", "dgrid", "util"};

    private int getScore(String item)
    {
        Map<String, Integer> scores = new HashMap<String, Integer>();
        scores.put("dojo", 5);
        scores.put("dijit", 4);
        scores.put("dojox", 2);
        scores.put("dgrid", 1);

        for(String key : scores.keySet())
        {
            if(item.indexOf(key) != -1)
            {
                return scores.get(key);
            }
        }

        return 0;
    }

    public String[] getChoicesFromFiles(PsiFile[] filesArray, String[] dojoLibraries, String module)
    {
        List<String> choices = new ArrayList<String>();

        for(int i=0;i<filesArray.length;i++)
        {
            PsiFile file = filesArray[i];

            PsiDirectory directory = file.getContainingDirectory();
            String result = directory.toString();

            // parse dojo libraries only
            int firstIndex = Integer.MAX_VALUE;
            String firstLibrary = null;

            for(String library : dojoLibraries)
            {
                int index = result.indexOf(library);
                if(index > -1 && index < firstIndex)
                {
                    firstIndex = index;
                    firstLibrary = library;
                }
            }

            if(firstLibrary != null)
            {
                String underscorePrefix = "";

                if(file.getName().startsWith("_"))
                {
                    underscorePrefix = "_";
                }

                result = result.substring(result.indexOf(firstLibrary));
                result = result.replace('\\', '/') + '/' + underscorePrefix + module;
                choices.add(result);
            }
        }

        Collections.sort(choices, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return getScore(o1) - getScore(o2);
            }
        });

        choices.add(module);
        return choices.toArray(new String[0]);
    }

    public String[] getPossibleDojoImports(PsiFile psiFile, String module)
    {
        PsiFile[] files = null;
        PsiFile[] filesWithUnderscore = null;

        try
        {
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

        return getChoicesFromFiles(filesArray, dojoLibraries, module);
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
            JSUtil.addStatementBeforeElement(parameters, parameters.getChildren()[0], MismatchedImportsDetector.defineToParameter(module) + ",", " ");
        }
    }

    public void addImport(PsiFile file, final String module)
    {
        JSRecursiveElementVisitor visitor = new DeclareFinder().getDefineVisitor(new DeclareFinder.CompletionCallback() {
            @Override
            public void run(Object[] result) {
                JSCallExpression callExpression = (JSCallExpression) result[0];
                JSFunction function = (JSFunction) result[1];

                createImport(module, (JSArrayLiteralExpression) callExpression.getArguments()[0], function.getParameterList());
            }
        });

        file.acceptChildren(visitor);
    }
}
