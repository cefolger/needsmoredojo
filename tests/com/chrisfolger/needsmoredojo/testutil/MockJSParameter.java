package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSParameterImpl;

import static org.mockito.Mockito.mock;

public class MockJSParameter extends JSParameterImpl
{
    String text = null;

    public MockJSParameter(String text) {
        super(mock(ASTNode.class));

        this.text = text;
    }

    @Override
    public String getText()
    {
        return text;
    }
}
