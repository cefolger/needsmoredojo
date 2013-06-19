package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;

import static org.mockito.Mockito.mock;

public class BasicPsiElements
{
    public static JSLiteralExpression Null()
    {
        return new MockJSLiteralExpression("null");
    }

    public static JSExpression expressionFromText(final String text)
    {
        return new JSExpressionImpl(mock(ASTNode.class)) {
            @Override
            public String getText()
            {
                return text;
            }
        };
    }
}
