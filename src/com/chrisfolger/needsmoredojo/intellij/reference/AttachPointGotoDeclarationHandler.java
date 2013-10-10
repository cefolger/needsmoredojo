package com.chrisfolger.needsmoredojo.intellij.reference;

import com.chrisfolger.needsmoredojo.core.amd.objectmodel.TemplatedWidgetUtil;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ServiceManager;
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
        if(psiElement == null || !psiElement.getLanguage().equals(Language.findLanguageByID("JavaScript")))
        {
            return new PsiElement[0];
        }

        DojoSettings settings = ServiceManager.getService(psiElement.getProject(), DojoSettings.class);
        if(!settings.isNeedsMoreDojoEnabled())
        {
            return new PsiElement[0];
        }

        PsiFile templateFile = new TemplatedWidgetUtil(psiElement.getContainingFile()).findTemplatePath();

        if(templateFile == null)
        {
            return new PsiElement[0];
        }

        PsiElement attachPoint = TemplatedWidgetUtil.getAttachPointElementInHtmlFile(psiElement, templateFile);
        if(attachPoint == null)
        {
            return new PsiElement[0];
        }

        return new PsiElement[] { attachPoint };
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext)
    {
        return "Jump to attach point";
    }
}
