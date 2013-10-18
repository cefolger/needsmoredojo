package com.chrisfolger.needsmoredojo.intellij.reference;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class MethodGotoDeclarationHandler implements GotoDeclarationHandler
{
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor)
    {
        if(psiElement == null || !psiElement.getLanguage().equals(Language.findLanguageByID("JavaScript")))
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

        if(!(psiElement.getPrevSibling().getPrevSibling() instanceof JSReferenceExpression))
        {
            return new PsiElement[0];
        }

        JSReferenceExpression referencedDefine = (JSReferenceExpression) psiElement.getPrevSibling().getPrevSibling();
        PsiElement resolvedDefine = AMDPsiUtil.resolveReferencedDefine(referencedDefine);
        if(resolvedDefine == null)
        {
            return new PsiElement[0];
        }

        // FIXME account for new StandbyWrapper({}).foo();
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

    @Nullable
    @Override
    public String getActionText(DataContext dataContext)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
