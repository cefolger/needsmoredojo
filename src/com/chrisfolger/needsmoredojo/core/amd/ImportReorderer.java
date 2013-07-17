package com.chrisfolger.needsmoredojo.core.amd;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.PsiElement;

public class ImportReorderer
{
    private JSLiteralExpression getNearestLiteralExpression(PsiElement element)
    {
        PsiElement node = element.getPrevSibling();
        int tries = 0;
        while(tries < 5)
        {
            if(node instanceof  JSLiteralExpression)
            {
                return (JSLiteralExpression) node;
            }

            node = node.getPrevSibling();
            tries ++;
        }

        return null;
    }

    public PsiElement[] getSourceAndDestination(PsiElement element)
    {
        JSLiteralExpression source = null;

        if(element instanceof JSLiteralExpression)
        {
            source = (JSLiteralExpression) element;
        }
        else
        {
            source = getNearestLiteralExpression(element);
        }

        // find destination
        JSLiteralExpression destination = getNearestLiteralExpression(source.getPrevSibling());
        if(destination == null)
        {
            return null;
        }

        return new PsiElement[] { source, destination };
    }

    public void reorder(PsiElement source, PsiElement destination)
    {
        destination.replace(source);
        source.replace(destination);
    }
}
