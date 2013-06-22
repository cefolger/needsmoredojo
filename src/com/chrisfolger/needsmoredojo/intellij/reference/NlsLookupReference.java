package com.chrisfolger.needsmoredojo.intellij.reference;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NlsLookupReference extends PsiReferenceBase<JSLiteralExpression> {
    private PsiElement element;

    public NlsLookupReference(JSLiteralExpression sourceElement, PsiElement nlsElement)
    {
        super(sourceElement);
        this.element = nlsElement;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return element; // TODO
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
