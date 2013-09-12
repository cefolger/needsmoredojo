package com.chrisfolger.needsmoredojo.core.util;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;

public class DefineUtil
{

    public DefineStatement getDefineStatementItemsFromArguments(JSExpression[] arguments)
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

        return new DefineStatement(literalExpressions, function, className);
    }

    public static PsiElement getNearestComma(PsiElement start)
    {
        PsiElement sibling = start.getPrevSibling();
        while(sibling != null && !(sibling instanceof JSLiteralExpression) && !(sibling instanceof JSParameter))
        {
            if(sibling.getText().equals(","))
            {
                return sibling;
            }

            sibling = sibling.getPrevSibling();
        }

        return null;
    }
}
