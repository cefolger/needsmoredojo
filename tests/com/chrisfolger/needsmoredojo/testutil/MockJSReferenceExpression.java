package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;

import static org.mockito.Mockito.mock;

public class MockJSReferenceExpression extends JSReferenceExpressionImpl
{
    private String referencedName;

    public MockJSReferenceExpression(String referencedName) {
        super(mock(ASTNode.class));

        this.referencedName = referencedName;
    }

    @Override
    public String getReferencedName()
    {
        return referencedName;
    }
}
