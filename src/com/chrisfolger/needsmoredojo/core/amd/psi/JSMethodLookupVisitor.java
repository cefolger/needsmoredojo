package com.chrisfolger.needsmoredojo.core.amd.psi;

import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;

public class JSMethodLookupVisitor extends JSRecursiveElementVisitor
{
    private String methodName;
    private boolean methodWasFound;
    private PsiElement foundElement;

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
            foundElement = node;
            return;
        }

        super.visitJSProperty(node);
    }

    @Override
    /**
     * the next case is a little fuzzier BUT a good approximation
     *
     * when you do this:
     *
     * module.method = ...
     * return module;
     *
     * We can be reasonably sure that "module" is the one you are referring to when you import the file, and "method"
     * is the name of the method. This structure that we need to check for is:
     *
     * JSDefinitionExpression
     *      JSReferenceExpression
     */
    public void visitJSReferenceExpression(JSReferenceExpression node)
    {
        if(node.getChildren().length != 1 || !(node.getChildren()[0] instanceof JSReferenceExpression))
        {
            super.visitJSReferenceExpression(node);
            return;
        }

        if(methodName.equals(node.getReferencedName()) && node.getParent() != null && node.getParent() instanceof JSDefinitionExpression)
        {
            methodWasFound = true;
            foundElement = node.getReferenceNameElement();
            return;
        }

        super.visitJSReferenceExpression(node);
    }

    public boolean isMethodWasFound() {
        return methodWasFound;
    }

    public PsiElement getFoundElement() {
        return foundElement;
    }
}
