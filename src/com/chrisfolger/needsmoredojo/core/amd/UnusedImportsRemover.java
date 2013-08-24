package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.util.DefineUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UnusedImportsRemover
{
    public class RemovalResult
    {
        private Set<PsiElement> elementsToDelete;
        private String deletedElementNames;

        public RemovalResult(Set<PsiElement> elementsToDelete, String deletedElementNames) {
            this.elementsToDelete = elementsToDelete;
            this.deletedElementNames = deletedElementNames;
        }

        public Set<PsiElement> getElementsToDelete() {
            return elementsToDelete;
        }

        public String getDeletedElementNames() {
            return deletedElementNames;
        }

        public String getDeletedElementsString()
        {
            String result = "";

            for(PsiElement element : elementsToDelete)
            {
                result += element.getText();
            }

            return result;
        }
    }

    public RemovalResult removeUnusedParameters(List<PsiElement> parameters, List<PsiElement> defines)
    {
        JSArrayLiteralExpression literal = (JSArrayLiteralExpression) defines.get(0).getParent();
        PsiElement function = parameters.get(0).getParent();

        final StringBuilder results = new StringBuilder();
        Set<PsiElement> elementsToDelete = new LinkedHashSet<PsiElement>();

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
                elementsToDelete.add(DefineUtil.getNearestComma(sibling));
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
                if(element instanceof LeafPsiElement)
                {
                    if(((LeafPsiElement) element).getTreeParent() == null)
                    {
                        continue;
                    }
                }
                element.delete();
            }
            catch(Exception e)
            {

            }
        }

        removeTrailingCommas(elementsToDelete, literal, function);


        RemovalResult result = new RemovalResult(elementsToDelete, results.toString());
        return result;
    }

    private void removeTrailingCommas(Set<PsiElement> deleteList, JSArrayLiteralExpression literal, PsiElement function)
    {
        try
        {
            PsiElement trailingComma = DefineUtil.getNearestComma(literal.getLastChild());
            if(trailingComma != null)
            {
                deleteList.add(trailingComma);
                trailingComma.delete();
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }

        /*
        at first this block was not here and for some reason trailing commas in the function argument list
        were still deleted. I'm not sure why, but I decided to make it explicit.
         */
        try
        {
            PsiElement trailingComma = DefineUtil.getNearestComma(function.getLastChild());
            if(trailingComma != null)
            {
                deleteList.add(trailingComma);
                trailingComma.delete();
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    // TODO detect registry.byNode and registry.byId

    /**
     * returns a visitor that will remove amd import literals and function parameters from their corresponding
     * lists if they are used in the code
     *
     * @param parameters the list of function parameters in a define statement
     * @param defines the list of literals in a define statement
     * @param exceptions a map of module : parameter representing exceptions. These modules will not be flagged as unused
     *  under any circumstance. Used for legacy dojo modules
     * @return the visitor
     */
    public JSRecursiveElementVisitor getVisitorToRemoveUsedModulesFromLists(final List<PsiElement> parameters, final List<PsiElement> defines, LinkedHashMap<String, String> exceptions)
    {
        final Collection<String> parameterExceptions = exceptions.values();

        JSRecursiveElementVisitor visitor = new JSRecursiveElementVisitor() {
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
        };

        return visitor;
    }

    public void removeSingleImport(@NotNull AMDImport amdImport)
    {
        JSArrayLiteralExpression literal = (JSArrayLiteralExpression) amdImport.getLiteral().getParent();
        PsiElement function = amdImport.getParameter().getParent();

        Set<PsiElement> elementsToDelete = new LinkedHashSet<PsiElement>();

        elementsToDelete.add(amdImport.getLiteral());
        elementsToDelete.add(amdImport.getParameter());

        // only remove commas at the end
        if(amdImport.getParameter().getNextSibling() != null && amdImport.getParameter().getNextSibling().getText().equals(","))
        {
            elementsToDelete.add(amdImport.getParameter().getNextSibling().getNextSibling());
        }

        // special case for when the element we're removing is last on the list
        PsiElement sibling = amdImport.getLiteral().getNextSibling();
        if(sibling != null && (sibling instanceof PsiWhiteSpace || sibling.getText().equals("]")))
        {
            elementsToDelete.add(DefineUtil.getNearestComma(sibling));
        }

        // only remove the next sibling if it's a comma
        PsiElement nextSibling = amdImport.getLiteral().getNextSibling();
        if(nextSibling != null && !nextSibling.getText().equals("]"))
        {
            elementsToDelete.add(amdImport.getLiteral().getNextSibling());
        }

        for(PsiElement element : elementsToDelete)
        {
            try
            {
                element.delete();
            }
            catch(Exception e)
            {
                // something happened, but it's probably not important when deleting.
            }
        }

        // TODO refactor slightly
        // TODO still need a test case for illegal argument being thrown

        removeTrailingCommas(elementsToDelete, literal, function);
    }
}
