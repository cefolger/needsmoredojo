package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.psi.PsiElement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicPsiElements
{
    public static PsiElement Null()
    {
        PsiElement element = mock(PsiElement.class);
        when(element.getText()).thenReturn("null");

        return element;
    }
}
