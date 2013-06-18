package com.chrisfolger.needsmoredojo.core.amd;

import com.intellij.lang.javascript.psi.JSNewExpression;
import com.intellij.lang.javascript.psi.JSRecursiveElementVisitor;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;

import java.util.ArrayList;
import java.util.List;

public class UnusedImportsRemover
{
    public class RemovalResult
    {
        private List<PsiElement> elementsToDelete;
        private String deletedElementNames;

        public RemovalResult(List<PsiElement> elementsToDelete, String deletedElementNames) {
            this.elementsToDelete = elementsToDelete;
            this.deletedElementNames = deletedElementNames;
        }

        public List<PsiElement> getElementsToDelete() {
            return elementsToDelete;
        }

        public String getDeletedElementNames() {
            return deletedElementNames;
        }
    }

    private PsiElement getNearestComma(PsiElement start)
    {
        PsiElement sibling = start.getPrevSibling();
        while(sibling != null)
        {
            if(sibling.getText().equals(","))
            {
                return sibling;
            }

            sibling = sibling.getPrevSibling();
        }

        return null;
    }

    public RemovalResult removeUnusedParameters(List<PsiElement> parameters, List<PsiElement> defines)
    {
        final StringBuilder results = new StringBuilder();
        List<PsiElement> elementsToDelete = new ArrayList<PsiElement>();

        for(PsiElement element : parameters)
        {
            elementsToDelete.add(element);

            PsiElement nextSibling = element.getNextSibling();

            // only remove commas at the end
            if(nextSibling != null && nextSibling.getText().equals(","))
            {
                elementsToDelete.add(element.getNextSibling());
            }
        }

        int current = 0;
        for(PsiElement element : defines)
        {
            elementsToDelete.add(element);
            if(results.toString().equals("") && current < 4)
            {
                results.append(element.getText());
            }
            else if (current < 4)
            {
                results.append("," + element.getText());
            }
            else if (current == 4)
            {
                results.append(String.format(" ... (+%d more) ", defines.size() - 4));
            }
            current++;

            // special case for when the element we're removing is last on the list
            PsiElement sibling = element.getNextSibling();
            if(sibling != null && (sibling instanceof PsiWhiteSpace || sibling.getText().equals("]")))
            {
                getNearestComma(sibling).delete();
            }

            // only remove the next sibling if it's a comma
            PsiElement nextSibling = element.getNextSibling();
            if(nextSibling != null && !nextSibling.getText().equals("]"))
            {
                elementsToDelete.add(element.getNextSibling());
            }
        }

        for(PsiElement element : elementsToDelete)
        {
            try
            {
                element.delete();
            }
            catch(Exception e)
            {

            }
        }

        RemovalResult result = new RemovalResult(elementsToDelete, results.toString());
        return result;
    }

    // TODO add exceptions?
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
