package com.chrisfolger.needsmoredojo.core.amd.importing.visitors;

import com.intellij.lang.javascript.psi.JSNewExpression;
import com.intellij.lang.javascript.psi.JSRecursiveElementVisitor;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.psi.PsiElement;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class UnusedImportsRemovalVisitor extends JSRecursiveElementVisitor
{
    private List<PsiElement> defines;
    private List<PsiElement> parameters;
    private Collection<String> parameterExceptions;
    private Set<PsiElement> visitedElements;

    public UnusedImportsRemovalVisitor(Set<PsiElement> visitedElements, List<PsiElement> defines, List<PsiElement> parameters, Collection<String> parameterExceptions)
    {
        super();

        this.visitedElements = visitedElements;
        this.defines = defines;
        this.parameters = parameters;
        this.parameterExceptions = parameterExceptions;
    }

    @Override
    public void visitJSReferenceExpression(JSReferenceExpression node)
    {
        if(this.visitedElements.contains(node))
        {
            super.visitJSReferenceExpression(node);
            return;
        }

        this.visitedElements.add(node);

        for(int i=0;i<parameters.size();i++)
        {
            if(parameterExceptions.contains(parameters.get(i).getText()))
            {
                parameters.remove(i);
                if(i < defines.size())
                {
                    defines.remove(i);
                }
                i--;
                continue;
            }

            if(node.getText().equals(parameters.get(i).getText()))
            {
                parameters.remove(i);

                if(i < defines.size())
                {
                    defines.remove(i);
                }
                i--;
            }
        }

        super.visitJSReferenceExpression(node);
    }

    @Override
    public void visitJSNewExpression(JSNewExpression node)
    {
        if(this.visitedElements.contains(node))
        {
            super.visitJSNewExpression(node);
            return;
        }

        this.visitedElements.add(node);

        for(int i=0;i<parameters.size();i++)
        {
            if(parameterExceptions.contains(parameters.get(i).getText()))
            {
                parameters.remove(i);
                if(i < defines.size())
                {
                    defines.remove(i);
                }
                i--;
                continue;
            }

            boolean used = false;
            if(node.getMethodExpression() != null && node.getMethodExpression().getText().equals(parameters.get(i).getText()))
            {
                used = true;
            }
            else if( node.getMethodExpression() == null && node.getText().startsWith("new " + parameters.get(i).getText()))
            {
                used = true;
            }

            if(used)
            {
                parameters.remove(i);
                if(i < defines.size())
                {
                    defines.remove(i);
                }
                i--;
            }
        }

        super.visitJSNewExpression(node);
    }
}
