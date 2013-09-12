package com.chrisfolger.needsmoredojo.core.amd.define;

import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
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

    public DefineStatement(JSArrayLiteralExpression arguments, JSFunctionExpression function, String className) {
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

    public @Nullable
    String getClassName() {
        return className;
    }
}
