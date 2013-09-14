package com.chrisfolger.needsmoredojo.core.amd.define.organizer;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;

class SortItem
{
    private PsiElement define;
    private PsiElement parameter;
    private boolean inactive;
    private PsiComment ignoreComment;
    private PsiComment regularComment;

    public SortItem(PsiElement define, PsiElement parameter, boolean inactive, PsiComment ignoreComment, PsiElement regularComment)
    {
        this.define = define;
        this.parameter = parameter;

        if(ignoreComment != null)
        {
            this.ignoreComment = (PsiComment) ignoreComment.copy();
        }

        if(regularComment != null)
        {
            this.regularComment = (PsiComment) regularComment.copy();
        }
    }

    PsiComment getRegularComment() {
        return regularComment;
    }

    public PsiComment getIgnoreComment() {
        return ignoreComment;
    }

    public PsiElement getDefine() {
        return define;
    }

    public void setDefine(PsiElement define) {
        this.define = define;
    }

    public PsiElement getParameter() {
        return parameter;
    }

    public void setParameter(PsiElement parameter) {
        this.parameter = parameter;
    }

    public boolean isInactive() {
        return inactive;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }
}