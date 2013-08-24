package com.chrisfolger.needsmoredojo.core.amd;

import com.intellij.lang.javascript.psi.JSElement;
import org.jetbrains.annotations.NotNull;

/**
 * represents the result of searching for the nearest AMD import
 */
public class AMDImport
{
    private JSElement literal;
    private JSElement parameter;

    public AMDImport(@NotNull JSElement literal, @NotNull JSElement parameter) {
        this.literal = literal;
        this.parameter = parameter;
    }

    public @NotNull JSElement getLiteral() {
        return literal;
    }

    public @NotNull JSElement getParameter() {
        return parameter;
    }
}
