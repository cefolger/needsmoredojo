package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.NotImplementedException;

import static org.mockito.Mockito.mock;

public class MockJSLiteralExpression extends JSLiteralExpressionImpl implements MockJSElementInterface, JSLiteralExpression
{
    private MockJSElementInterface nextSibling;
    private String text = null;
    private MockJSElementInterface prevSibling;

    public MockJSLiteralExpression(String text) {
        super(mock(ASTNode.class));

        this.text = text;
    }

    @Override
    public String getText()
    {
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
}
