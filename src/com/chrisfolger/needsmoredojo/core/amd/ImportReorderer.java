package com.chrisfolger.needsmoredojo.core.amd;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.PsiElement;

public class ImportReorderer
{
    public PsiElement[] getSourceAndDestination(PsiElement element)
    {
        // TODO move to testable section
        // test cases:
        // comma resolves to real element
        if(element.getText().equals(","))
        {
            element = element.getPrevSibling();
        }

        JSLiteralExpression source = (JSLiteralExpression) element;
        // find destination
        PsiElement node = source.getPrevSibling();
        int tries = 0;
        while(tries < 5)
        {
            if(node instanceof  JSLiteralExpression)
            {
                break;
            }

            node = node.getPrevSibling();
            tries ++;
        }

        if(!(node instanceof JSLiteralExpression))
        {
            return null;
        }

        JSLiteralExpression destination = (JSLiteralExpression) node;
        int i=0;

        return new PsiElement[] { source, destination };
    }

    public void reorder(PsiElement source, PsiElement destination)
    {
        destination.replace(source);
        source.replace(destination);
    }
}
