package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSArrayLiteralExpressionImpl;

import static org.mockito.Mockito.mock;

public class MockJSArrayLiteralExpression extends JSArrayLiteralExpressionImpl
{
    private JSExpression[] expressions;

    public MockJSArrayLiteralExpression(String[] defines) {
        super(mock(ASTNode.class));

        expressions = new JSExpression[defines.length];
        for(int i=0;i<defines.length;i++)
        {
            expressions[i] = new MockJSLiteralExpression(defines[i]);
        }
    }

    @Override
    public JSExpression[] getExpressions()
    {
        return expressions;
    }
}
