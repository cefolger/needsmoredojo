package com.chrisfolger.needsmoredojo.core.amd.objectmodel;

import com.intellij.psi.PsiElement;

public class AMDValidator
{
    public static final String DOJO_LEGACY_DECLARE = "dojo.declare";
    public static final String DOJO_DECLARE = "declare";

    public static boolean isDeclareFunction(PsiElement element)
    {
        return element.getText().equals(DOJO_DECLARE) || element.getText().equals(DOJO_LEGACY_DECLARE);
    }
}
