package com.chrisfolger.needsmoredojo.core.amd.objectmodel;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import org.jetbrains.annotations.Nullable;

public class DeclareStatementItems
{
    private JSExpression[] expressionsToMixin;
    private JSProperty[] methodsToConvert;
    private JSElement declareContainingStatement;
    private JSLiteralExpression className;

    public DeclareStatementItems(JSExpression[] expressionsToMixin, JSProperty[] methodsToConvert, JSElement returnStatement) {
        this.expressionsToMixin = expressionsToMixin;
        this.methodsToConvert = methodsToConvert;
        this.declareContainingStatement = returnStatement;
    }

    public DeclareStatementItems(JSLiteralExpression className, JSExpression[] expressionsToMixin, JSProperty[] methodsToConvert, JSElement declareContainingStatement) {
        this(expressionsToMixin, methodsToConvert, declareContainingStatement);

        this.className = className;
    }

    @Nullable
    public JSLiteralExpression getClassName() {
        return className;
    }

    public JSElement getDeclareContainingStatement() {
        return declareContainingStatement;
    }

    public JSExpression[] getExpressionsToMixin() {
        return expressionsToMixin;
    }

    public JSProperty[] getMethodsToConvert() {
        return methodsToConvert;
    }
}
