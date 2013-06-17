package com.chrisfolger.needsmoredojo.base;

import com.chrisfolger.needsmoredojo.conventions.MismatchedImportsDetector;
import com.chrisfolger.needsmoredojo.refactoring.DeclareFinder;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

public class ImportCreator
{
    public String[] getPossibleDojoImports(PsiFile psiFile, String module)
    {
        PsiFile[] files = FilenameIndex.getFilesByName(psiFile.getProject(), module + ".js", GlobalSearchScope.allScope(psiFile.getProject()));
        String[] choices = new String[files.length + 1];
        choices[choices.length - 1] = module;

        String[] dojoLibraries = new String[] { "dojo", "dijit", "dojox", "dgrid", "util"};

        for(int i=0;i<files.length;i++)
        {
            PsiFile file = files[i];
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
                result = result.substring(result.indexOf(firstLibrary));
                result = result.replace('\\', '/') + '/' + module;
                choices[i] = result;
            }
        }

        return choices;
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
