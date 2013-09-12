package com.chrisfolger.needsmoredojo.core.amd.psi;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;

public class AMDPsiUtil
{
    public enum Direction
    {
        UP,
        DOWN
    }

    public static PsiElement getDefineForVariable(PsiFile file, String textToCompare)
    {
        List<PsiElement> defines = new ArrayList<PsiElement>();
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        new DefineResolver().gatherDefineAndParameters(file, defines, parameters);

        for(int i=0;i<parameters.size();i++)
        {
            if(i > defines.size() - 1)
            {
                return null; // amd import is being modified
            }

            if(parameters.get(i).getText().equals(textToCompare))
            {
                return defines.get(i);
            }
        }

        return null;
    }

    public static PsiElement getNearestComma(PsiElement start)
    {
        PsiElement sibling = start.getPrevSibling();
        while(sibling != null && !(sibling instanceof JSLiteralExpression) && !(sibling instanceof JSParameter))
        {
            if(sibling.getText().equals(","))
            {
                return sibling;
            }

            sibling = sibling.getPrevSibling();
        }

        return null;
    }

    public static JSLiteralExpression getNearestLiteralExpression(PsiElement element, Direction direction)
    {
        PsiElement node = element;
        if(direction == Direction.UP)
        {
            node = element.getPrevSibling();
        }
        else
        {
            node = element.getNextSibling();
        }

        int tries = 0;
        while(tries < 5)
        {
            if(node instanceof  JSLiteralExpression)
            {
                return (JSLiteralExpression) node;
            }

            if(node == null)
            {
                return null;
            }

            if(direction == Direction.UP)
            {
                node = node.getPrevSibling();
            }
            else
            {
                node = node.getNextSibling();
            }

            tries ++;
        }

        return null;
    }
}
