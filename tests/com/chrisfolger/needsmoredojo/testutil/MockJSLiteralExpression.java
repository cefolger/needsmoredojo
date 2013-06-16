package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl;

import static org.mockito.Mockito.mock;

public class MockJSLiteralExpression extends JSLiteralExpressionImpl
{
    private String text = null;

    public MockJSLiteralExpression(String text) {
        super(mock(ASTNode.class));

        this.text = text;
    }

    @Override
    public String getText()
    {
        return text;
    }
}
