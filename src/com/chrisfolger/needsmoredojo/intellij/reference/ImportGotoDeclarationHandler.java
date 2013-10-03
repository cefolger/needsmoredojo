package com.chrisfolger.needsmoredojo.intellij.reference;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourceLibrary;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportResolver;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
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
public class ImportGotoDeclarationHandler implements GotoDeclarationHandler
{
    private PsiFile resolveReferencedFile(Project project, PsiElement define)
    {
        ImportResolver resolver = new ImportResolver();
        String text = define.getText().replaceAll("'|\"", "");
        PsiFile[] possibleFiles = resolver.getPossibleDojoImportFiles(project, NameResolver.getModuleName(text), false, false);

        LinkedHashMap<String, PsiFile> possibleImportedFiles = resolver.getChoicesFromFiles(possibleFiles,
                new SourcesLocator().getSourceLibraries(project).toArray(new SourceLibrary[0]),
                NameResolver.getModuleName(text),
                define.getContainingFile(), false, true);

        for(String importString : possibleImportedFiles.keySet())
        {
            if(importString.equals(text))
            {
                return possibleImportedFiles.get(importString);
            }
        }

        return null;
    }

    private PsiElement resolveReferencedDefine(PsiElement psiElement)
    {
        boolean isReference = psiElement instanceof JSReferenceExpression || (psiElement.getParent() != null && psiElement.getParent() instanceof JSReferenceExpression);
        boolean isNew = psiElement instanceof JSNewExpression || (psiElement.getParent() != null && psiElement.getParent() instanceof JSNewExpression);

        // support for reference or new expression
        if(!(isReference || isNew))
        {
            return null;
        }

        DefineResolver resolver = new DefineResolver();
        DefineStatement defineStatement = resolver.getNearestImportBlock(psiElement);
        for (int x = 0; x < defineStatement.getFunction().getParameters().length; x++)
        {
            JSParameter parameter = defineStatement.getFunction().getParameters()[x];
            JSExpression define = defineStatement.getArguments().getExpressions()[x];

            if(parameter.getText().equals(psiElement.getText()))
            {
                return define;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor)
    {
        if(psiElement == null || !psiElement.getLanguage().equals(Language.findLanguageByID("JavaScript")))
        {
            return new PsiElement[0];
        }

        PsiElement referencedDefine = resolveReferencedDefine(psiElement);
        if(referencedDefine == null)
        {
            return new PsiElement[0];
        }

        PsiFile referencedFile = resolveReferencedFile(psiElement.getProject(), referencedDefine);

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
