package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.impl.JSFunctionImpl;

import static org.mockito.Mockito.mock;

public class MockJSFunction extends JSFunctionImpl
{
    private JSParameter[] parameters = null;

    public MockJSFunction(String[] parameters) {
        super(mock(ASTNode.class));

        this.parameters = new JSParameter[parameters.length];
        for(int i=0;i<parameters.length;i++)
        {
            this.parameters[i] = new MockJSParameter(parameters[i]);
        }
    }

    @Override
    public JSParameter[] getParameters()
    {
        return parameters;
    }
}
