package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.psi.tree.IElementType;

import static org.mockito.Mockito.mock;

public class MockJSReferenceExpression extends JSReferenceExpressionImpl
{
    private String referencedName;

    public MockJSReferenceExpression(String referencedName) {
        super(mock(IElementType.class));

        this.referencedName = referencedName;
    }

    @Override
    public String getReferencedName()
    {
        return referencedName;
    }
}
