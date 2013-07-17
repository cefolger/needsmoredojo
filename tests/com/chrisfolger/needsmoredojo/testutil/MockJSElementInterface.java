package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.psi.PsiElement;

public interface MockJSElementInterface extends PsiElement
{
    public MockJSElementInterface comesBefore(MockJSElementInterface element);

    public void setPrevSibling(MockJSElementInterface element);
    public void setNextSibling(MockJSElementInterface element);
    public PsiElement getLastChild();
    public void setParent(MockJSElementInterface element);
}
