package com.chrisfolger.needsmoredojo.base;

import com.chrisfolger.needsmoredojo.refactoring.DeclareFinder;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class ImportCreator
{
    protected void createImport(String module, JSArrayLiteralExpression imports, JSParameterList parameters)
    {
        PsiElement importElement = JSUtil.createExpression(imports, String.format("'%s'", module));
        PsiElement parameterElement = JSUtil.createExpression(parameters, String.format("%s", module));

        imports.addBefore(importElement, imports.getChildren()[0]);
        parameters.addBefore(importElement, parameters.getChildren()[0]);
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
