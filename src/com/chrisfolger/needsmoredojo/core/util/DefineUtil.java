package com.chrisfolger.needsmoredojo.core.util;

import com.intellij.lang.javascript.psi.*;
import org.jetbrains.annotations.Nullable;

public class DefineUtil
{
    public class DefineStatementItems
    {
        private JSArrayLiteralExpression arguments;
        private JSFunctionExpression function;
        private String className;

        public DefineStatementItems(JSArrayLiteralExpression arguments, JSFunctionExpression function, String className) {
            this.arguments = arguments;
            this.function = function;
            this.className = className;
        }

        public JSArrayLiteralExpression getArguments() {
            return arguments;
        }

        public JSFunctionExpression getFunction() {
            return function;
        }

        public @Nullable String getClassName() {
            return className;
        }
    }

    public DefineStatementItems getDefineStatementItemsFromArguments(JSExpression[] arguments)
    {
        // account for when we get this (even though this is defined as legacy) :
        /**
         * define('classname', [], function(...){});
         */
        int argumentOffset = 0;
        String className = null;

        if(arguments.length > 1 && arguments[0] instanceof JSLiteralExpression && arguments[1] instanceof JSArrayLiteralExpression)
        {
            argumentOffset = 1;
            className = arguments[0].getText();
        }
        else if(!(arguments.length > 1 && arguments[0] instanceof JSArrayLiteralExpression && arguments[1] instanceof JSFunctionExpression))
        {
            return null;
        }

        // get the first argument which should be an array literal
        JSArrayLiteralExpression literalExpressions = (JSArrayLiteralExpression) arguments[0 + argumentOffset];

        // get the second argument which should be a function
        JSFunctionExpression function = (JSFunctionExpression) arguments[1 + argumentOffset];

        return new DefineStatementItems(literalExpressions, function, className);
    }
}
