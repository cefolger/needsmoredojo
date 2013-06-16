package com.chrisfolger.needsmoredojo.refactoring;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSReturnStatement;
import com.intellij.lang.javascript.psi.JSVarStatement;

public class ClassConverter implements DeclareFinder.CompletionCallback
{
    @Override
    public void run(Object[] result)
    {
        JSReturnStatement returnStatement = (JSReturnStatement) result[0];
        JSCallExpression declaration = (JSCallExpression) result[1];


    }
}
