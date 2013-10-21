package com.chrisfolger.needsmoredojo.intellij.reference;

import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Normally, IntelliJ will resolve this.inherited and look up all of the references to this.inherited in other
 * modules. This GotoDeclarationHandler will attempt to resolve this.inherited calls by the enclosing method name,
 * and will search the dependency graph for references.
 */
public class ThisInheritedGotoDeclarationHandler extends DojoDeclarationHandler implements GotoDeclarationHandler
{
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor)
    {
        if(psiElement == null || !psiElement.getLanguage().equals(Language.findLanguageByID("JavaScript")))
        {
            return new PsiElement[0];
        }

        if(!isEnabled(psiElement.getProject()))
        {
            return new PsiElement[0];
        }

        if(!(psiElement.getParent() instanceof JSReferenceExpression))
        {
            return new PsiElement[0];
        }

        if(!psiElement.getText().equals("inherited"))
        {
            return new PsiElement[0];
        }

        // get the enclosing function of the psiElement
        PsiElement parent = psiElement.getParent();
        JSProperty owningFunction = null;

        while(parent != null)
        {
            if(parent instanceof JSProperty)
            {
                owningFunction = (JSProperty) parent;
            }

            parent = parent.getParent();
        }

        Set<PsiElement> resolvedMethods = AMDPsiUtil.resolveInheritedMethod(psiElement.getContainingFile(), psiElement.getProject(), owningFunction.getName(), 0);
        return resolvedMethods.toArray(new PsiElement[resolvedMethods.size()]);
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext)
    {
        return "Goto this.inherited module";
    }
}
