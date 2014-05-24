package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.impl.JSFunctionExpressionImpl;
import org.jetbrains.annotations.NotNull;

import static org.mockito.Mockito.mock;

public class MockJSFunctionExpression extends JSFunctionExpressionImpl
{
    private JSFunction function;
    private JSParameter[] parameters;

    public MockJSFunctionExpression(String[] parameters)
    {
        super(mock(ASTNode.class));

        function = new MockJSFunction(parameters);

        this.parameters = new JSParameter[parameters.length];
        for(int i=0;i<parameters.length;i++)
        {
            this.parameters[i] = new MockJSParameter(parameters[i]);
        }
    }

    @NotNull
    @Override
    public JSParameter[] getParameters()
    {
        return parameters;
    }
}
