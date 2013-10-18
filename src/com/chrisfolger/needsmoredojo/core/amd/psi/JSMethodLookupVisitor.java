package com.chrisfolger.needsmoredojo.core.amd.psi;

import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSRecursiveElementVisitor;

public class JSMethodLookupVisitor extends JSRecursiveElementVisitor
{
    private String methodName;
    private boolean methodWasFound;
    private JSProperty foundProperty;

    public JSMethodLookupVisitor(String methodName)
    {
        this.methodName = methodName;
    }

    @Override
    /**
     * the first case we want to check for is when a method is declared as a property like this:
     *
     * declare(..., {
     *     method1: ...
     * }
     */
    public void visitJSProperty(JSProperty node)
    {
        if(node.getName() == null || !node.getName().equals(methodName))
        {
            super.visitJSProperty(node);
            return;
        }

        // check if the definition is an actual function
        if(node.getValue() instanceof JSFunctionExpression)
        {
            methodWasFound = true;
            foundProperty = node;
            return;
        }

        super.visitJSProperty(node);
    }

    public boolean isMethodWasFound() {
        return methodWasFound;
    }

    public JSProperty getFoundProperty() {
        return foundProperty;
    }
}
