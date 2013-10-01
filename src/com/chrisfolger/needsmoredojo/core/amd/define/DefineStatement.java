package com.chrisfolger.needsmoredojo.core.amd.define;

import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a dojo define block
 */
public class DefineStatement
{
    private JSArrayLiteralExpression arguments;
    private JSFunctionExpression function;
    private String className;
    private JSCallExpression callExpression;

    public DefineStatement(JSArrayLiteralExpression arguments, JSFunctionExpression function, String className, JSCallExpression originalParent) {
        this.arguments = arguments;
        this.function = function;
        this.className = className;
        this.callExpression = originalParent;
    }

    public JSArrayLiteralExpression getArguments() {
        return arguments;
    }

    public JSFunctionExpression getFunction() {
        return function;
    }

    public @Nullable
    String getClassName() {
        return className;
    }

    public JSCallExpression getCallExpression() {
        if(callExpression == null)
        {
            throw new RuntimeException("callExpression was not set but is being accessed");
        }

        return callExpression;
    }
}
