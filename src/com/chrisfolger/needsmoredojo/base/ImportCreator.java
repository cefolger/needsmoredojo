package com.chrisfolger.needsmoredojo.base;

import com.chrisfolger.needsmoredojo.conventions.MismatchedImportsDetector;
import com.chrisfolger.needsmoredojo.refactoring.DeclareFinder;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class ImportCreator
{
    protected void createImport(String module, JSArrayLiteralExpression imports, JSParameterList parameters)
    {
        JSUtil.addStatementBeforeElement(imports, imports.getChildren()[0], String.format("'%s',", module), "\n");
        JSUtil.addStatementBeforeElement(parameters, parameters.getChildren()[0], MismatchedImportsDetector.defineToParameter(module) + ",", " ");
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
