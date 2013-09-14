package com.chrisfolger.needsmoredojo.core.amd.define.organizer;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;

public class SortedPsiElementAdapter
{
    private PsiElement element;
    private boolean inactive;
    private PsiComment ignoreComment;

    public SortedPsiElementAdapter(PsiElement element, boolean inactive, PsiComment ignoreComment) {
        this.element = element;
        this.inactive = inactive;
        this.ignoreComment = ignoreComment;
    }

    public static SortedPsiElementAdapter fromParameter(SortItem item)
    {
        SortedPsiElementAdapter adapter = new SortedPsiElementAdapter(item.getParameter(), item.isInactive(), item.getIgnoreComment());
        return adapter;
    }

    public static SortedPsiElementAdapter fromDefine(SortItem item)
    {
        SortedPsiElementAdapter adapter = new SortedPsiElementAdapter(item.getDefine(), item.isInactive(), item.getIgnoreComment());
        return adapter;
    }

    public PsiComment getIgnoreComment() {
        return ignoreComment;
    }

    public PsiElement getElement() {
        return element;
    }

    public void setElement(PsiElement element) {
        this.element = element;
    }

    public boolean isInactive() {
        return inactive;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }
}
