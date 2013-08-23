package com.chrisfolger.needsmoredojo.core.amd;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.psi.PsiElement;

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

    public LocatedAMDImport findNearestImport(JSElement elementAtCaretPosition)
    {
        return null;
    }
}
