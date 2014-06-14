package com.chrisfolger.needsmoredojo.core.amd.define;

import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.psi.PsiElement;
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
    private PsiElement classNameElement;

    public DefineStatement(JSArrayLiteralExpression arguments, JSFunctionExpression function, String className, JSCallExpression originalParent) {
        this.arguments = arguments;
        this.function = function;
        this.className = className;
        this.callExpression = originalParent;
    }

    public DefineStatement(JSArrayLiteralExpression arguments, JSFunctionExpression function, String className, JSCallExpression originalParent, PsiElement classNameElement) {
        this(arguments, function, className, originalParent);
        this.classNameElement = classNameElement;
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

    @Nullable
    public PsiElement getClassNameElement() {
        return classNameElement;
    }

    public JSCallExpression getCallExpression() {
        if(callExpression == null)
        {
            throw new RuntimeException("callExpression was not set but is being accessed");
        }

        return callExpression;
    }
}
