package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSDefinitionExpressionImpl;

import static org.mockito.Mockito.mock;

public class MockJSDefinitionExpressionWithIndexedProperty extends JSDefinitionExpressionImpl
{
    private String definitionPath;
    private String definitionName;

    public MockJSDefinitionExpressionWithIndexedProperty(String definitionPath, String definitionName) {
        super(mock(ASTNode.class));

        this.definitionPath = definitionPath;
        this.definitionName = definitionName;
    }

    @Override
    public JSExpression getExpression()
    {
        return new MockJSIndexedPropertyAccessExpression(definitionName);
    }
}
