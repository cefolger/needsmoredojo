package com.chrisfolger.needsmoredojo.intellij.reference;

import com.chrisfolger.needsmoredojo.core.amd.objectmodel.TemplatedWidgetUtil;
import com.chrisfolger.needsmoredojo.intellij.actions.JumpToAttachPointAction;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class AttachPointGotoDeclarationHandler implements GotoDeclarationHandler
{
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor)
    {
        PsiFile templateFile = new TemplatedWidgetUtil(psiElement.getContainingFile()).findTemplatePath();

        PsiElement attachPoint = JumpToAttachPointAction.getAttachPointElementInHtmlFile(psiElement, templateFile, false);

        return new PsiElement[] { attachPoint };
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext)
    {
        return "testing";
    }
}
