package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSIndexedPropertyAccessExpressionImpl;
import com.intellij.psi.tree.IElementType;

import static org.mockito.Mockito.mock;

public class MockJSIndexedPropertyAccessExpression extends JSIndexedPropertyAccessExpressionImpl
{
    private String value;

    public MockJSIndexedPropertyAccessExpression(String propertyValue) {
        super(mock(IElementType.class));

        this.value = propertyValue;
    }

    @Override
    public JSExpression getIndexExpression()
    {
        return BasicPsiElements.expressionFromText(this.value);
    }
}
