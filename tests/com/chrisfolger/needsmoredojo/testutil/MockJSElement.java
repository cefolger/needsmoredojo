package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.impl.JSElementImpl;
import com.intellij.psi.PsiElement;

import static org.mockito.Mockito.mock;

public class MockJSElement extends JSElementImpl
{
    private String text;
    private MockJSElement nextSibling;
    private PsiElement prevSibling;

    public MockJSElement(String text) {
        super(mock(ASTNode.class));

        this.text = text;
    }

    @Override
    public String getText()
    {
        return text;
    }

    @Override
    public PsiElement getNextSibling()
    {
        return nextSibling;
    }

    @Override
    public PsiElement getPrevSibling()
    {
        return prevSibling;
    }

    public MockJSElement comesAfter(MockJSElement element)
    {
        element.prevSibling = this;
        this.nextSibling = element;

        return this;
    }

    public MockJSElement comesBefore(MockJSElement element)
    {
        element.nextSibling = this;
        this.prevSibling = element;

        return this;
    }
}
