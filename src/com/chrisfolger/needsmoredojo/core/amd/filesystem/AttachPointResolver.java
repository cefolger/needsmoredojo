package com.chrisfolger.needsmoredojo.core.amd.filesystem;

import com.chrisfolger.needsmoredojo.core.amd.objectmodel.TemplatedWidgetUtil;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.lang.Language;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class AttachPointResolver
{
    @Nullable
    public static PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement psiElement, int i, @Nullable Editor editor)
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

}
