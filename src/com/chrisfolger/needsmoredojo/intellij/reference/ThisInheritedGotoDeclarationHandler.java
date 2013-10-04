package com.chrisfolger.needsmoredojo.intellij.reference;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.DeclareResolver;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.DeclareStatementItems;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ThisInheritedGotoDeclarationHandler implements GotoDeclarationHandler
{
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor)
    {
        if(psiElement == null || !psiElement.getLanguage().equals(Language.findLanguageByID("JavaScript")))
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

        // get the modules that this module inherits
        DeclareResolver declareResolver = new DeclareResolver();

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

        PsiElement resolvedMethod = resolveInheritedMethod(psiElement.getContainingFile(), psiElement.getProject(), owningFunction.getName());
        if(resolvedMethod != null)
        {
            return new PsiElement[] { resolvedMethod };
        }

        return new PsiElement[0];
    }

    private PsiElement resolveInheritedMethod(PsiFile file, Project project, String methodName)
    {
        DeclareStatementItems declareObject = new DeclareResolver().getDeclareObject(file);

        // FIXME: go all the way up the dependency graph!
        DojoModuleFileResolver resolver = new DojoModuleFileResolver();
        // search each inherited module starting from the last one for an equivalent property that matches.
        for (int x = declareObject.getExpressionsToMixin().length - 1; x >= 0; x--)
        {
            JSExpression expression = declareObject.getExpressionsToMixin()[x];

            PsiElement resolvedDefine = AMDPsiUtil.resolveReferencedDefine(expression);
            if(resolvedDefine == null) continue;

            PsiFile resolvedFile = resolver.resolveReferencedFile(project, resolvedDefine);
            if(resolvedFile == null) continue;

            JSProperty method = fileHasMethod(resolvedFile, methodName);
            if(method != null)
            {
                return method;
            }
            else
            {
                // attempt to recursively search the dependency graph
            }
        }

        return null;
    }

    private @Nullable JSProperty fileHasMethod(PsiFile file, String methodName)
    {
        // FIXME null checks
        DeclareStatementItems declareObject = new DeclareResolver().getDeclareObject(file);

        for(JSProperty property : declareObject.getMethodsToConvert())
        {
            if(property.getName().equals(methodName))
            {
                return property;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext)
    {
        return "Goto this.inherited module";
    }
}
