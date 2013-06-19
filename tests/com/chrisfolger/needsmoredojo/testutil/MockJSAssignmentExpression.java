package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.lang.javascript.psi.impl.JSAssignmentExpressionImpl;

import static org.mockito.Mockito.mock;

public class MockJSAssignmentExpression extends JSAssignmentExpressionImpl
{
    private String definitionNamespace;
    private String definitionName;
    private String content;

    public MockJSAssignmentExpression(String definitionNamespace, String definitionName, String content) {
        super(mock(ASTNode.class));

        this.definitionNamespace = definitionNamespace;
        this.definitionName = definitionName;
        this.content = content;
    }

    @Override
    public JSExpression[] getChildren()
    {
        return new JSExpression[] { new MockJSDefinitionExpression(definitionNamespace, definitionName), BasicPsiElements.expressionFromText(content) };
    }
}
