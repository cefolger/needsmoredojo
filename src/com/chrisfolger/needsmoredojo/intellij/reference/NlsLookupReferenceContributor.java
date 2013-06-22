package com.chrisfolger.needsmoredojo.intellij.reference;

import com.intellij.javascript.JavaScriptReferenceContributor;
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.PatternUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

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

                    // get the list of defines
                    // find one that matches
                    // check to see if it's an i18n file
                    // resolve the reference to the file
                    int i=0;
                }

                return new PsiReference[0];  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }
}
