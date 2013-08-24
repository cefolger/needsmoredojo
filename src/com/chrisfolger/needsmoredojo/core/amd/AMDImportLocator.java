package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.util.DefineStatement;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

/**
 * Meant to find the nearest AMD import (define literal + parameter) based on the user's caret position
 *
 */
public class AMDImportLocator
{
    // have to have this because getParameter can call getDefine recursively and vice versa, so we don't a stack overflow
    // if one of them is null
    private int iterations = 0;

    protected @Nullable JSElement getParameter(PsiElement elementAtCaretPosition, DefineStatement defineStatement)
    {
        iterations += 1;

        if(elementAtCaretPosition == null || iterations > 10)
        {
            return null;
        }

        if(elementAtCaretPosition.getPrevSibling() instanceof JSParameter)
        {
            return (JSElement) elementAtCaretPosition.getPrevSibling();
        }

        if(elementAtCaretPosition.getParent() instanceof JSParameter)
        {
            return (JSElement) elementAtCaretPosition.getParent();
        }

        // assume the caret element is a define literal
        JSElement define = getDefineLiteral(elementAtCaretPosition, defineStatement);
        if(define == null)
        {
            return null;
        }

        int defineIndex = getIndexOfDefine(defineStatement, define);
        JSElement parameter = defineStatement.getFunction().getParameters()[defineIndex];

        return parameter;
    }

    protected @Nullable JSElement getDefineLiteral(PsiElement elementAtCaretPosition, DefineStatement defineStatement)
    {
        iterations += 1;

        if(elementAtCaretPosition == null || iterations > 10)
        {
            return null;
        }

        if(elementAtCaretPosition.getParent() instanceof JSLiteralExpression)
        {
            return (JSElement) elementAtCaretPosition.getParent();
        }

        if(elementAtCaretPosition.getPrevSibling() instanceof JSLiteralExpression)
        {
            return (JSElement) elementAtCaretPosition.getPrevSibling();
        }

        // if none of the above cases work, we assume this is a parameter and find its corresponding literal
        JSElement parameter = getParameter(elementAtCaretPosition, defineStatement);
        if(parameter == null)
        {
            return null;
        }

        int parameterIndex = getIndexOfParameter(defineStatement, parameter);
        return defineStatement.getArguments().getExpressions()[parameterIndex];
    }

    private int getIndexOfDefine(DefineStatement defineStatement, JSElement literal)
    {
        for(int i=0; i<defineStatement.getArguments().getExpressions().length;i++)
        {
            if(defineStatement.getArguments().getExpressions()[i].getText().equals(literal.getText()))
            {
                return i;
            }
        }

        return -1;
    }

    private int getIndexOfParameter(DefineStatement defineStatement, JSElement parameter)
    {
        for(int i=0;i<defineStatement.getFunction().getParameters().length;i++)
        {
            if(defineStatement.getFunction().getParameters()[i].getText().equals(parameter.getText()))
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Entry point for this class. Locates the AMD import define literal and parameter that the user has selected
     *
     * @param elementAtCaretPosition
     * @param file
     * @return null if either the define literal or the parameter is null
     */
    public @Nullable AMDImport findNearestImport(PsiElement elementAtCaretPosition, PsiFile file)
    {
        DefineStatement defineStatement = new DeclareFinder().getDefineStatementItems(file);

        JSElement defineLiteral = getDefineLiteral(elementAtCaretPosition, defineStatement);
        JSElement parameter = getParameter(elementAtCaretPosition, defineStatement);

        if(defineLiteral == null || parameter == null)
        {
            return null;
        }

        return new AMDImport(defineLiteral, parameter);
    }
}
