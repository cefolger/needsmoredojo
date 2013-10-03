package com.chrisfolger.needsmoredojo.core.amd.importing;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.importing.visitors.UnusedImportsRemovalVisitor;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSRecursiveElementVisitor;
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
     * @param exceptions a map of module : parameter representing exceptions. These modules will not be flagged as unused
     *  under any circumstance. Used for legacy dojo modules
     * @return the visitor
     */
    public List<UnusedImportBlockEntry> filterUsedModules(PsiFile file, final LinkedHashMap<String, String> exceptions)
    {
        final Collection<String> parameterExceptions = exceptions.values();

        final Set<JSCallExpression> listOfDefinesOrRequiresToVisit = new DefineResolver().getAllImportBlocks(file);
        List<UnusedImportBlockEntry> results = new ArrayList<UnusedImportBlockEntry>();

        JSCallExpression[] expressions = listOfDefinesOrRequiresToVisit.toArray(new JSCallExpression[listOfDefinesOrRequiresToVisit.size()]);
        for (int i = expressions.length - 1; i >= 0; i--)
        {
            List<PsiElement> blockDefines = new ArrayList<PsiElement>();
            List<PsiElement> blockParameters = new ArrayList<PsiElement>();

            try {
                new DefineResolver().addDefinesAndParametersOfImportBlock(expressions[i], blockDefines, blockParameters);
            } catch (InvalidDefineException e) {

            }

            Set<String> terminators = new HashSet<String>();
            terminators.add(",");
            for(int x=0;x<blockDefines.size();x++)
            {
                PsiElement element = blockDefines.get(x);
                PsiElement ignoreComment = AMDPsiUtil.getNextElementOfType(element, PsiComment.class, terminators, new HashSet<String>());
                if(ignoreComment != null && ignoreComment.getText().equals(IGNORE_COMMENT))
                {
                    blockDefines.remove(x);
                    if(x < blockParameters.size())
                    {
                        blockParameters.remove(x);
                    }
                    x--;
                }
            }

            JSRecursiveElementVisitor visitor = new UnusedImportsRemovalVisitor(blockDefines, blockParameters, parameterExceptions, expressions[i]);
            expressions[i].accept(visitor);

            results.add(new UnusedImportBlockEntry(expressions[i].getMethodExpression().getText().equals("define"), expressions[i], blockDefines, blockParameters));
        }

        return results;
    }
}
