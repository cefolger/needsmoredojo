package com.chrisfolger.needsmoredojo.core.amd.importing;

import com.chrisfolger.needsmoredojo.core.amd.importing.visitors.UnusedImportsRemovalVisitor;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;

import java.util.*;

public class UnusedImportsRemover
{
    public static String IGNORE_COMMENT = "/*NMD:Ignore*/";

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
                if(element == null) continue;
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
            AMDPsiUtil.removeParameter(element, elementsToDelete);
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

            AMDPsiUtil.removeDefineLiteral(element, elementsToDelete);
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

        AMDPsiUtil.removeTrailingCommas(elementsToDelete, literal, function);

        RemovalResult result = new RemovalResult(elementsToDelete, results.toString());
        return result;
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
    public void filterUnusedModules(PsiFile file, final List<PsiElement> parameters, final List<PsiElement> defines, final LinkedHashMap<String, String> exceptions)
    {
        final Collection<String> parameterExceptions = exceptions.values();

        Set<String> terminators = new HashSet<String>();
        terminators.add(",");
        for(int i=0;i<defines.size();i++)
        {
            PsiElement element = defines.get(i);
            PsiElement ignoreComment = AMDPsiUtil.getNextElementOfType(element, PsiComment.class, terminators, new HashSet<String>());
            if(ignoreComment != null && ignoreComment.getText().equals(IGNORE_COMMENT))
            {
                defines.remove(i);
                if(i < parameters.size())
                {
                    parameters.remove(i);
                }
                i--;
            }
        }

        JSRecursiveElementVisitor visitor = new UnusedImportsRemovalVisitor(defines, parameters, parameterExceptions);

        file.accept(visitor);
    }
}
