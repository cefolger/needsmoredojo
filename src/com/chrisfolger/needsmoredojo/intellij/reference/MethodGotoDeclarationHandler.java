package com.chrisfolger.needsmoredojo.intellij.reference;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSThisExpression;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * This class looks up methods of off dojo modules. If you have domConstruct.empty for example, it will determine
 * that domConstruct references dojo/dom-construct and search for an "empty" method in that file to create a
 * reference for it.
 */
public class MethodGotoDeclarationHandler extends DojoDeclarationHandler implements GotoDeclarationHandler
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

        if(!(psiElement.getParent() != null && psiElement.getParent() instanceof JSReferenceExpression))
        {
            return new PsiElement[0];
        }

        if(psiElement.getPrevSibling() == null || psiElement.getPrevSibling().getPrevSibling() == null)
        {
            return new PsiElement[0];
        }

        if(!(psiElement.getPrevSibling().getPrevSibling() instanceof JSReferenceExpression) && !(psiElement.getPrevSibling().getPrevSibling() instanceof JSThisExpression))
        {
            return new PsiElement[0];
        }

        PsiElement prevPrevSibling = psiElement.getPrevSibling().getPrevSibling();

        /**
         * this case occurs when the user is trying to navigate ... declaration on a method off of this. At this point
         * we can search its base classes for the method in question.
         */
        if(prevPrevSibling instanceof JSThisExpression)
        {
            if(psiElement.getText().equals("inherited"))
            {
                return new PsiElement[0];
            }

            // if the method is defined in this file, return it instead of searching other files. Rely on this.inherited
            // references for that behavior.
            PsiElement resolvedInThisFile = AMDPsiUtil.fileHasMethod(psiElement.getContainingFile(), psiElement.getText(), false);
            if(resolvedInThisFile != null)
            {
                return new PsiElement[] { resolvedInThisFile };
            }
            else
            {
                Set<PsiElement> resolvedMethods = AMDPsiUtil.resolveInheritedMethod(psiElement.getContainingFile(), psiElement.getProject(), psiElement.getText(), 0);
                return resolvedMethods.toArray(new PsiElement[resolvedMethods.size()]);
            }
        }
        /**
         * the second case is when the user is referencing a method off of another dojo module. In this case, we
         * use a less accurate approach because the module in question might not be a standard module that defines
         * its methods in a nice object literal.
         */
        else
        {
            JSReferenceExpression referencedDefine = (JSReferenceExpression) prevPrevSibling;
            PsiElement resolvedDefine = AMDPsiUtil.resolveReferencedDefine(referencedDefine);
            if(resolvedDefine == null)
            {
                return new PsiElement[0];
            }

            DojoModuleFileResolver resolver = new DojoModuleFileResolver();
            PsiFile resolvedFile = resolver.resolveReferencedFile(psiElement.getProject(), resolvedDefine);

            if(resolvedFile == null)
            {
                return new PsiElement[0];
            }

            String methodName = psiElement.getText();
            PsiElement method = AMDPsiUtil.fileHasMethod(resolvedFile, methodName, true);
            if(method != null)
            {
                // found it!
                return new PsiElement[] { method };
            }
            else
            {
                // didn't find it!
                return new PsiElement[0];
            }
        }
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
