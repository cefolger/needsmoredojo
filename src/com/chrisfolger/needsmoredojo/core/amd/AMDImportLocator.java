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
    private int iterations = 0;

    /**
     * represents the result of searching for the nearest AMD import
     */
    public class LocatedAMDImport
    {
        private JSElement literal;
        private JSElement parameter;

        public LocatedAMDImport(JSElement literal, JSElement parameter) {
            this.literal = literal;
            this.parameter = parameter;
        }

        public JSElement getLiteral() {
            return literal;
        }

        public JSElement getParameter() {
            return parameter;
        }
    }

    protected @Nullable JSElement getParameter(PsiElement elementAtCaretPosition, DefineStatement defineStatement)
    {
        iterations += 1;

        if(elementAtCaretPosition == null || iterations > 2)
        {
            return null;
        }

        if(elementAtCaretPosition.getText().equals(",") && elementAtCaretPosition.getPrevSibling() instanceof JSParameter)
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

        if(elementAtCaretPosition == null || iterations > 2)
        {
            return null;
        }

        if(elementAtCaretPosition.getParent() instanceof JSLiteralExpression)
        {
            return (JSElement) elementAtCaretPosition.getParent();
        }

        // special case
        if(elementAtCaretPosition.getText().equals(",") && elementAtCaretPosition.getPrevSibling() instanceof JSLiteralExpression)
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
     * @return
     */
    public LocatedAMDImport findNearestImport(PsiElement elementAtCaretPosition, PsiFile file)
    {
        DefineStatement defineStatement = new DeclareFinder().getDefineStatementItems(file);

        JSElement defineLiteral = getDefineLiteral(elementAtCaretPosition, defineStatement);
        JSElement parameter = getParameter(elementAtCaretPosition, defineStatement);

        return new LocatedAMDImport(defineLiteral, parameter);
    }
}
