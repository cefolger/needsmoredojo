package com.chrisfolger.needsmoredojo.core.amd.importing.visitors;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class UnusedImportsRemovalVisitor extends JSRecursiveElementVisitor
{
    private List<PsiElement> defines;
    private List<PsiElement> parameters;
    private Collection<String> parameterExceptions;
    private JSCallExpression owningImportBlock;
    private DefineResolver resolver;

    public UnusedImportsRemovalVisitor(List<PsiElement> defines, List<PsiElement> parameters, Collection<String> parameterExceptions, JSCallExpression owningImportBlock)
    {
        super();

        this.resolver = new DefineResolver();
        this.defines = defines;
        this.parameters = parameters;
        this.parameterExceptions = parameterExceptions;
        this.owningImportBlock = owningImportBlock;
    }

    private boolean isOwnedByCurrentImportBlock(PsiElement node, String nodeText, String prefix, boolean exact)
    {
        DefineStatement block = resolver.getNearestImportBlock(node);
        if(block == null)
        {
            return true;
        }

        while(block != null)
        {
            // if the nearest block contains a reference AND it is not equal to the owning block, then the owning
            // block cannot claim this reference
            for(JSParameter parameter : block.getFunction().getParameters())
            {
                String matchText = prefix + parameter.getText();

                if( (exact && matchText.equals(nodeText)) || (!exact && matchText.startsWith(nodeText)) )
                {
                    return owningImportBlock.equals(block.getCallExpression());
                }
            }

            block = resolver.getNearestImportBlock(block.getCallExpression());
        }

        return true;
    }

    @Override
    public void visitJSReferenceExpression(JSReferenceExpression node)
    {
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

            if(node.getText().equals(parameters.get(i).getText()) && isOwnedByCurrentImportBlock(node, node.getText(), "", true))
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
            boolean isOwned = false;

            if(node.getMethodExpression() != null && node.getMethodExpression().getText().equals(parameters.get(i).getText()))
            {
                used = true;
                isOwned = isOwnedByCurrentImportBlock(node.getMethodExpression(), node.getMethodExpression().getText(), "", true);
            }
            else if( node.getMethodExpression() == null && node.getText().startsWith("new " + parameters.get(i).getText()))
            {
                used = true;
                isOwned = isOwnedByCurrentImportBlock(node, node.getText(), "new ", false);
            }

            if(used && isOwned)
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
