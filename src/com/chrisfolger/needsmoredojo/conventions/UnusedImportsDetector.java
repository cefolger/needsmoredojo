package com.chrisfolger.needsmoredojo.conventions;

import com.intellij.lang.javascript.psi.JSNewExpression;
import com.intellij.lang.javascript.psi.JSRecursiveElementVisitor;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.psi.PsiElement;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Chris
 * Date: 1/1/13
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class UnusedImportsDetector
{
    // TODO add exceptions?
    // TODO type inference
    // TODO closure context coloring
    // TODO detect registry.byNode and registry.byId
    public JSRecursiveElementVisitor getVisitorToRemoveUsedParameters(final List<PsiElement> parameters, final List<PsiElement> defines)
    {
        JSRecursiveElementVisitor visitor = new JSRecursiveElementVisitor() {
            @Override
            public void visitJSReferenceExpression(JSReferenceExpression node)
            {
                for(int i=0;i<parameters.size();i++)
                {
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
                for(int i=0;i<parameters.size();i++)
                {
                    if(node.getText().startsWith("new " + parameters.get(i).getText()))
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
        };

        return visitor;
    }
}
