package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSArrayLiteralExpressionImpl;
import org.apache.commons.lang.NotImplementedException;

import static org.mockito.Mockito.mock;

public class MockJSArrayLiteralExpression extends JSArrayLiteralExpressionImpl implements MockJSElementInterface
{
    private JSExpression[] expressions;
    private JSElement lastChild;
    private MockJSElement bracket;

    public MockJSArrayLiteralExpression(String[] defines) {
        super(mock(ASTNode.class));

        expressions = new JSExpression[defines.length];
        for(int i=0;i<defines.length;i++)
        {
            expressions[i] = new MockJSLiteralExpression(defines[i]);
        }
    }

    public MockJSElement getBracket() {
        return bracket;
    }

    public void setBracket(MockJSElement bracket) {
        this.bracket = bracket;
    }

    public void setExpressions(JSExpression[] expressions) {
        this.expressions = expressions;
    }

    @Override
    public MockJSElementInterface comesBefore(MockJSElementInterface element) {
        throw new NotImplementedException();
    }

    @Override
    public void setPrevSibling(MockJSElementInterface element) {
        throw new NotImplementedException();
    }

    @Override
    public void setNextSibling(MockJSElementInterface element) {
        throw new NotImplementedException();
    }

    @Override
    public JSElement getLastChild() {
        return lastChild;
    }

    @Override
    public void setParent(MockJSElementInterface element) {
        throw new NotImplementedException();
    }

    @Override
    public String printTree() {
        return MockJSElementInterfaceUtil.printTree(this);
    }

    public void setLastChild(JSElement lastChild) {
        this.lastChild = lastChild;
    }

    @Override
    public JSExpression[] getExpressions()
    {
        return expressions;
    }
}
