package com.chrisfolger.needsmoredojo.intellij.reference;

import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.intellij.javascript.JavaScriptReferenceContributor;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PatternUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NlsLookupReferenceContributor extends JavaScriptReferenceContributor
{
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar)
    {
        ElementPattern<JSLiteralExpression> pattern = PlatformPatterns.psiElement(JSLiteralExpression.class);

        registrar.registerReferenceProvider(pattern, new PsiReferenceProvider() {
            @NotNull
            @Override
            public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                PsiElement parent = psiElement.getParent();
                if(parent instanceof JSIndexedPropertyAccessExpression) {
                    JSIndexedPropertyAccessExpression accessor = (JSIndexedPropertyAccessExpression) parent;
                    PsiElement qualifier = accessor.getQualifier();

                    JSLiteralExpression literal = (JSLiteralExpression) psiElement;
                    if(!literal.isQuotedLiteral())
                    {
                        return new PsiReference[0];
                    }

                    return new PsiReference[] { new NlsLookupReference(qualifier, accessor, (JSLiteralExpression) psiElement) };
                }

                return new PsiReference[0];  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }
}
