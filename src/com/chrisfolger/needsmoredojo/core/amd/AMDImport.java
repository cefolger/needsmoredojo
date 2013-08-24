package com.chrisfolger.needsmoredojo.core.amd;

import com.intellij.lang.javascript.psi.JSElement;

/**
 * represents the result of searching for the nearest AMD import
 */
public class AMDImport
{
    private JSElement literal;
    private JSElement parameter;

    public AMDImport(JSElement literal, JSElement parameter) {
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
