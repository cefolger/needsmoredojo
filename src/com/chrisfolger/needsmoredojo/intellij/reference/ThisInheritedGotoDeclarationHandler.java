package com.chrisfolger.needsmoredojo.intellij.reference;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.DeclareResolver;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.DeclareStatementItems;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.chrisfolger.needsmoredojo.intellij.inspections.DojoInspection;
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
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Normally, IntelliJ will resolve this.inherited and look up all of the references to this.inherited in other
 * modules. This GotoDeclarationHandler will attempt to resolve this.inherited calls by the enclosing method name,
 * and will search the dependency graph for references.
 */
public class ThisInheritedGotoDeclarationHandler extends DojoDeclarationHandler implements GotoDeclarationHandler
{
    private int DEPTH_LIMIT = 10;

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

        Set<PsiElement> resolvedMethods = resolveInheritedMethod(psiElement.getContainingFile(), psiElement.getProject(), owningFunction.getName(), 0);
        return resolvedMethods.toArray(new PsiElement[resolvedMethods.size()]);
    }

    private @NotNull Set<PsiElement> resolveInheritedMethod(PsiFile file, Project project, String methodName, int currentDepth)
    {
        Set<PsiElement> resolvedMethods = new LinkedHashSet<PsiElement>();
        DeclareStatementItems declareObject = new DeclareResolver().getDeclareObject(file);

        if(declareObject == null || declareObject.getExpressionsToMixin() == null)
        {
            return resolvedMethods;
        }

        DojoModuleFileResolver resolver = new DojoModuleFileResolver();
        // search each inherited module starting from the last one for an equivalent property that matches.
        for (int x = declareObject.getExpressionsToMixin().length - 1; x >= 0; x--)
        {
            JSExpression expression = declareObject.getExpressionsToMixin()[x];

            PsiElement resolvedDefine = AMDPsiUtil.resolveReferencedDefine(expression);
            if(resolvedDefine == null) continue;

            PsiFile resolvedFile = resolver.resolveReferencedFile(project, resolvedDefine);
            if(resolvedFile == null) continue;

            PsiElement method = AMDPsiUtil.fileHasMethod(resolvedFile, methodName, false);
            if(method != null)
            {
                resolvedMethods.add(method);
            }
            else
            {
                Set<PsiElement> inheritedMethod = resolveInheritedMethod(resolvedFile, project, methodName, currentDepth+1);
                if(inheritedMethod != null && currentDepth < DEPTH_LIMIT)
                {
                    for(PsiElement element : inheritedMethod)
                    {
                        resolvedMethods.add(element);
                    }
                }
            }
        }

        Logger.getLogger(ThisInheritedGotoDeclarationHandler.class).trace("depth for " + methodName + " in " + file.getVirtualFile().getCanonicalPath() + ": " + currentDepth);
        return resolvedMethods;
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext)
    {
        return "Goto this.inherited module";
    }
}
