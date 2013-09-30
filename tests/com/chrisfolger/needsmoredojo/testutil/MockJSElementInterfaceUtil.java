package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.psi.PsiElement;

public class MockJSElementInterfaceUtil
{
    public static String printTree(MockJSElementInterface element)
    {
        PsiElement nextSibling = element.getNextSibling();
        String result = element.getText();

        while(nextSibling != null)
        {
            result += nextSibling.getText();
            nextSibling = nextSibling.getNextSibling();
        }

        return result;
    }
}
