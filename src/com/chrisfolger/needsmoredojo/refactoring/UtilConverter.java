package com.chrisfolger.needsmoredojo.refactoring;

import com.intellij.lang.javascript.psi.JSCallExpression;

public class UtilConverter implements DeclareFinder.CompletionCallback
{
    @Override
    public void run(Object result)
    {
        JSCallExpression expression = (JSCallExpression) result;

        // now we need to get the object literal with all of the function names

    }
}
