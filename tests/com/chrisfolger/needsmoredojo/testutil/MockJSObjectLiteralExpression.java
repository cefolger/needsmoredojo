package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.impl.JSObjectLiteralExpressionImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class MockJSObjectLiteralExpression extends JSObjectLiteralExpressionImpl
{
    private Map<String, String> properties;

    public MockJSObjectLiteralExpression(Map<String, String> properties) {
        super(mock(ASTNode.class));

        this.properties = properties;
    }

    @Override
    public JSProperty[] getProperties()
    {
        List<JSProperty> props = new ArrayList<JSProperty>();

        for(Map.Entry<String, String> entry : properties.entrySet())
        {
            JSProperty property = mock(JSProperty.class); // TODO
            props.add(property);
        }

        return props.toArray(new JSProperty[0]);
    }
}
