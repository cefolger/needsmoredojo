package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSArrayLiteralExpressionImpl;

import static org.mockito.Mockito.mock;

public class MockJSArrayLiteralExpression extends JSArrayLiteralExpressionImpl
{
    private JSExpression[] expressions;
    private JSElement lastChild;

    public MockJSArrayLiteralExpression(String[] defines) {
        super(mock(ASTNode.class));

        expressions = new JSExpression[defines.length];
        for(int i=0;i<defines.length;i++)
        {
            expressions[i] = new MockJSLiteralExpression(defines[i]);
        }
    }

    public void setExpressions(JSExpression[] expressions) {
        this.expressions = expressions;
    }

    @Override
    public JSElement getLastChild() {
        return lastChild;
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
