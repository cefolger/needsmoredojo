package com.chrisfolger.needsmoredojo.core.util;

import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class DeclareUtil
{
    public static final String DOJO_LEGACY_DECLARE = "dojo.declare";
    public static final String DOJO_DECLARE = "declare";

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

    public DeclareStatementItems getDeclareStatementFromParsedStatement(Object[] result)
    {
        return getDeclareStatementFromParsedStatement(result, true);
    }

    public DeclareStatementItems getDeclareStatementFromParsedStatement(Object[] result, boolean parseMethodsFromObjectLiteral)
    {
        JSCallExpression expression = (JSCallExpression) result[0];

        // this will be used to determine what we mixin to the util
        JSExpression[] expressionsToMixin = new JSExpression[0];

        /*
            so many different possibilities...

            declare(null, {});
            declare([], {});
            declare(string, [], {});
            declare(string, mixin, {});
            declare(mixin, {});

            dojo.declare(...) (legacy)
         */
        int objectLiteralIndex = 1;
        if(expression.getArguments()[0] instanceof JSArrayLiteralExpression)
        {
            JSArrayLiteralExpression arrayLiteral = (JSArrayLiteralExpression) expression.getArguments()[0];
            expressionsToMixin = arrayLiteral.getExpressions();
        }
        else if (expression.getArguments()[0] instanceof JSReferenceExpression)
        {
            expressionsToMixin = new JSExpression[] { expression.getArguments()[0] };
        }
        else if (expression.getArguments().length == 3 && expression.getArguments()[1] instanceof JSReferenceExpression)
        {
            expressionsToMixin = new JSExpression[] { expression.getArguments()[1] };
            objectLiteralIndex = 2;
        }
        else if (expression.getArguments().length == 3 && expression.getArguments()[1] instanceof JSArrayLiteralExpression)
        {
            JSArrayLiteralExpression arrayLiteral = (JSArrayLiteralExpression) expression.getArguments()[1];
            expressionsToMixin = arrayLiteral.getExpressions();
            objectLiteralIndex = 2;
        }

        JSLiteralExpression className = null;
        if(expression.getArguments()[0] instanceof JSLiteralExpression)
        {
            className = (JSLiteralExpression) expression.getArguments()[0];
        }

        // now we need to get the object literal with all of the function names
        JSObjectLiteralExpression literal = (JSObjectLiteralExpression) expression.getArguments()[objectLiteralIndex];
        JSProperty[] methodsToConvert = literal.getProperties();

        return new DeclareStatementItems(className, expressionsToMixin, methodsToConvert, (JSElement) result[1]);
    }

    public static boolean isDeclareFunction(PsiElement element)
    {
        return element.getText().equals(DOJO_DECLARE) || element.getText().equals(DOJO_LEGACY_DECLARE);
    }
}
