package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.impl.JSFunctionExpressionImpl;
import com.intellij.lang.javascript.psi.stubs.JSFunctionStubBase;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.mockito.Mockito.mock;

public class MockJSFunctionExpression extends JSFunctionExpressionImpl<JSFunctionStubBase>
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
