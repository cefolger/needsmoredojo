package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.impl.JSElementImpl;
import com.intellij.psi.PsiElement;

import static org.mockito.Mockito.mock;

public class MockJSElement extends JSElementImpl implements  MockJSElementInterface
{
    private String text;
    private MockJSElementInterface nextSibling;
    private MockJSElementInterface prevSibling;
    private MockJSElementInterface parent;
    private MockJSElementInterface lastChild;

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
        return (PsiElement) nextSibling;
    }

    @Override
    public void delete()
    {
        if(prevSibling != null && prevSibling instanceof MockJSElementInterface)
        {
            MockJSElementInterface sibling = (MockJSElementInterface) prevSibling;
            sibling.setNextSibling(this.nextSibling);
        }

        if(nextSibling != null && nextSibling instanceof MockJSElementInterface)
        {
            MockJSElementInterface sibling = nextSibling;
            sibling.setPrevSibling(this.prevSibling);
        }
    }

    @Override
    public PsiElement getPrevSibling()
    {
        return (PsiElement) prevSibling;
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
        return (JSElement) this.lastChild;
    }

    @Override
    public void setParent(MockJSElementInterface element) {
        this.parent = element;
    }

    @Override
    public JSElement getParent()
    {
        return (JSElement) this.parent;
    }

    public MockJSElementInterface comesBefore(MockJSElementInterface element)
    {
        element.setPrevSibling(this);
        this.nextSibling = element;

        return this;
    }

    @Override
    public void setPrevSibling(MockJSElementInterface element) {
        this.prevSibling = element;
    }

    @Override
    public void setNextSibling(MockJSElementInterface element) {
        this.nextSibling = element;
    }

    public MockJSElement comesAfter(MockJSElement element)
    {
        element.nextSibling = this;
        this.prevSibling = element;

        return this;
    }

    @Override
    public String printTree() {
        return MockJSElementInterfaceUtil.printTree(this);
    }
}
