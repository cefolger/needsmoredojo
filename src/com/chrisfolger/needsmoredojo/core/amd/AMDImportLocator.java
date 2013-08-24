package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.util.DefineStatement;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.PsiFile;

/**
 * Meant to find the nearest AMD import (define literal + parameter) based on the user's caret position
 *
 */
public class AMDImportLocator
{
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

    protected JSElement getParameter(JSElement elementAtCaretPosition, DefineStatement defineStatement)
    {
        return null;
    }

    protected JSElement getDefineLiteral(JSElement elementAtCaretPosition, DefineStatement defineStatement)
    {
        if(elementAtCaretPosition == null)
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

        return null;
    }

    public LocatedAMDImport findNearestImport(JSElement elementAtCaretPosition, PsiFile file)
    {
        return null;
    }
}
