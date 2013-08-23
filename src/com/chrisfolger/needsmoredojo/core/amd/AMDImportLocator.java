package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.util.DefineStatement;
import com.intellij.lang.javascript.psi.JSElement;
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

    protected JSElement getDefineLiteral(JSElement elementAtCaretPosition, DefineStatement defineStatement)
    {
        return null;
    }

    public LocatedAMDImport findNearestImport(JSElement elementAtCaretPosition, PsiFile file)
    {
        return null;
    }
}
