package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.util.DefineUtil;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSParameter;
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

    private int getIndexInParent(PsiElement element)
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

    public PsiElement[] getSourceAndDestination(PsiElement element)
    {
        JSLiteralExpression source = null;

        if(element instanceof JSLiteralExpression)
        {
            source = (JSLiteralExpression) element;
        }
        else if (element.getParent() instanceof JSLiteralExpression)
        {
            source = (JSLiteralExpression) element.getParent();
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

    public void doSwap(PsiElement source)
    {
        PsiElement[] defines = getSourceAndDestination(source);

        // get the parameter element
        JSArgumentList list = (JSArgumentList) defines[0].getParent().getParent();
        DefineUtil.DefineStatementItems items = new DefineUtil().getDefineStatementItemsFromArguments(list.getArguments());

        int sourceIndex = getIndexInParent(defines[0]);
        int destinationIndex = getIndexInParent(defines[1]);
        JSParameter[] parameterList = items.getFunction().getParameters();

        PsiElement[] parameters = new PsiElement[] { parameterList[sourceIndex], parameterList[destinationIndex] };

        reorder(defines[0], defines[1]);
        reorder(parameters[0], parameters[1]);
    }
}
