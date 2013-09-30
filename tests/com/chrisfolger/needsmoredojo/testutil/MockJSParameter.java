package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSParameterImpl;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.NotImplementedException;

import static org.mockito.Mockito.mock;

public class MockJSParameter extends JSParameterImpl implements MockJSElementInterface
{
    private MockJSElementInterface prevSibling;
    private MockJSElementInterface nextSibling;
    String text = null;

    public MockJSParameter(String text) {
        super(mock(ASTNode.class));

        this.text = text;
    }

    @Override
    public String getText()
    {
        return text;
    }

    @Override
    public MockJSElementInterface comesBefore(MockJSElementInterface element) {
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

    @Override
    public void setParent(MockJSElementInterface element) {
        throw new NotImplementedException();
    }

    @Override
    public PsiElement getPrevSibling()
    {
        return this.prevSibling;
    }

    @Override
    public PsiElement getNextSibling()
    {
        return this.nextSibling;
    }

    @Override
    public String printTree() {
        return MockJSElementInterfaceUtil.printTree(this);
    }
}
