package com.chrisfolger.needsmoredojo.intellij.reference;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourceLibrary;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportResolver;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.chrisfolger.needsmoredojo.intellij.inspections.DojoInspection;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSNewExpression;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;

/**
 * This action provides Goto ... declaration functionality for any AMD module references.
 *
 * There's some logic in here that I could have separated out, but since it is not reusable I just kept
 * it in here for the moment. In the future it can be separated out if necessary.
 */
public class ImportGotoDeclarationHandler extends DojoDeclarationHandler implements GotoDeclarationHandler
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

        PsiElement referencedDefine = AMDPsiUtil.resolveReferencedDefine(psiElement);
        if(referencedDefine == null)
        {
            return new PsiElement[0];
        }

        PsiFile referencedFile = new DojoModuleFileResolver().resolveReferencedFile(psiElement.getProject(), referencedDefine);

        if(referencedFile == null)
        {
            return new PsiElement[0];
        }

        return new PsiElement[] { referencedFile };
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext)
    {
        return "jump to AMD import";
    }
}
