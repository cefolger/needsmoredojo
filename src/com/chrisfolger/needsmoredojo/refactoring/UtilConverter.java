package com.chrisfolger.needsmoredojo.refactoring;

import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;

public class UtilConverter implements DeclareFinder.CompletionCallback
{
    @Override
    public void run(Object[] result)
    {
        JSCallExpression expression = (JSCallExpression) result[0];
        JSReturnStatement returnStatement = (JSReturnStatement) result[1];

        // this will be used to determine what we mixin to the util
        JSArrayLiteralExpression arrayLiteral = (JSArrayLiteralExpression) expression.getArguments()[0];
        JSExpression[] expressionsToMixin = arrayLiteral.getExpressions();

        // now we need to get the object literal with all of the function names
        JSObjectLiteralExpression literal = (JSObjectLiteralExpression) expression.getArguments()[1];
        JSProperty[] methodsToConvert = literal.getProperties();

        doRefactor(returnStatement, expressionsToMixin, methodsToConvert);
    }

    public void doRefactor(JSReturnStatement originalReturnStatement, JSExpression[] mixins, JSProperty[] properties)
    {
        // insert new items before this
        PsiElement parent = originalReturnStatement.getParent();

        JSExpressionCodeFragment fragment = JSElementFactory.createExpressionCodeFragment(parent.getProject(), "var x = y", parent, true);
    }
}
