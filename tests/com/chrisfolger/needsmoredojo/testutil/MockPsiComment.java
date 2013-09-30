package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import org.apache.commons.lang.NotImplementedException;

public class MockPsiComment extends PsiCommentImpl implements MockJSElementInterface
{
    private MockJSElementInterface nextSibling;
    private String text = null;
    private MockJSElementInterface prevSibling;

    public MockPsiComment(String text) {
        super(null, text);
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
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
        throw new NotImplementedException();
    }

    @Override
    public void setParent(MockJSElementInterface element) {
        throw new NotImplementedException();
    }

    @Override
    public String printTree() {
        return MockJSElementInterfaceUtil.printTree(this);
    }
}
