package com.chrisfolger.needsmoredojo.refactoring;

import com.intellij.lang.javascript.psi.*;

import java.util.List;

public class ClassConverter implements DeclareFinder.CompletionCallback
{
    @Override
    public void run(Object[] result)
    {
        JSReturnStatement returnStatement = (JSReturnStatement) result[0];
        JSCallExpression declaration = (JSCallExpression) result[1];
        List<JSExpressionStatement> methods = (List<JSExpressionStatement>) result[2];

        JSExpression[] mixins = ((JSArrayLiteralExpression) declaration.getArguments()[0]).getExpressions();

        doRefactor(mixins, methods, returnStatement);
    }

    public void doRefactor(JSExpression[] mixins, List<JSExpressionStatement> methods, JSReturnStatement originalReturnStatement)
    {

    }
}
