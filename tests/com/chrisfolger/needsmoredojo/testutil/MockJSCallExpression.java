package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSCallExpressionImpl;

import static org.mockito.Mockito.mock;

public class MockJSCallExpression extends JSCallExpressionImpl
{
    private JSExpression[] arguments;

    public MockJSCallExpression(JSExpression[] arguments) {
        super(mock(ASTNode.class));

        this.arguments = arguments;
    }

    @Override
    public JSExpression[] getArguments()
    {
        return arguments;
    }
}
