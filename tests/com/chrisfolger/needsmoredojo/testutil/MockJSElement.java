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
    private JSElement parent;
    private JSElement lastChild;

    public MockJSElement(String text) {
        super(mock(ASTNode.class));

        this.text = text;
    }

    public void setParent(JSElement parent) {
        this.parent = parent;
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
    public void delete()
    {
        if(prevSibling != null && prevSibling instanceof MockJSElement)
        {
            MockJSElement sibling = (MockJSElement) prevSibling;
            sibling.nextSibling = this.nextSibling;
        }

        if(nextSibling != null && nextSibling instanceof MockJSElement)
        {
            MockJSElement sibling = nextSibling;
            sibling.prevSibling = this.prevSibling;
        }
    }

    @Override
    public PsiElement getPrevSibling()
    {
        return prevSibling;
    }

    public JSElement isLastChildOf(MockJSElement element)
    {
        element.lastChild = this;
        this.parent = element;

        return this;
    }

    public MockJSElement isParentOf(MockJSElement element)
    {
        element.parent = this;
        return this;
    }

    @Override
    public JSElement getLastChild()
    {
        return this.lastChild;
    }

    @Override
    public JSElement getParent()
    {
        return this.parent;
    }

    public MockJSElement comesBefore(MockJSElement element)
    {
        element.prevSibling = this;
        this.nextSibling = element;

        return this;
    }

    public MockJSElement comesAfter(MockJSElement element)
    {
        element.nextSibling = this;
        this.prevSibling = element;

        return this;
    }
}
