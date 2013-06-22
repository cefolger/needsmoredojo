package com.chrisfolger.needsmoredojo.core.util;

import com.intellij.lang.javascript.psi.*;
import org.jetbrains.annotations.Nullable;

public class DeclareUtil
{
    public class DeclareStatementItems
    {
        private JSExpression[] expressionsToMixin;
        private JSProperty[] methodsToConvert;
        private JSReturnStatement returnStatement;
        private JSLiteralExpression className;

        public DeclareStatementItems(JSExpression[] expressionsToMixin, JSProperty[] methodsToConvert, JSReturnStatement returnStatement) {
            this.expressionsToMixin = expressionsToMixin;
            this.methodsToConvert = methodsToConvert;
            this.returnStatement = returnStatement;
        }

        public DeclareStatementItems(JSLiteralExpression className, JSExpression[] expressionsToMixin, JSProperty[] methodsToConvert, JSReturnStatement returnStatement) {
            this(expressionsToMixin, methodsToConvert, returnStatement);

            this.className = className;
        }

        @Nullable
        public JSLiteralExpression getClassName() {
            return className;
        }

        public JSReturnStatement getReturnStatement() {
            return returnStatement;
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
        JSReturnStatement returnStatement = (JSReturnStatement) result[1];

        // this will be used to determine what we mixin to the util
        JSExpression[] expressionsToMixin = new JSExpression[0];

        /*
            three possible syntax'es for declare:

            declare(null, {});
            declare([], {});
            declare(string, [], {});
         */
        int objectLiteralIndex = 1;
        if(expression.getArguments()[0] instanceof JSArrayLiteralExpression)
        {
            JSArrayLiteralExpression arrayLiteral = (JSArrayLiteralExpression) expression.getArguments()[0];
            expressionsToMixin = arrayLiteral.getExpressions();
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

        return new DeclareStatementItems(className, expressionsToMixin, methodsToConvert, returnStatement);
    }
}
