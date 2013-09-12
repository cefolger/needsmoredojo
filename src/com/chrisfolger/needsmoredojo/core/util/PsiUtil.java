package com.chrisfolger.needsmoredojo.core.util;

import com.intellij.psi.PsiElement;

public class PsiUtil
{
    public static int getIndexInParent(PsiElement element)
    {
        for(int i=0;i< element.getParent().getChildren().length;i++)
        {
            if(element.getParent().getChildren()[i] == element)
            {
                return i;
            }
        }

        return -1;
    }
}
