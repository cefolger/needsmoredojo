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
    private JSDefinitionExpression definition;

    public MockJSAssignmentExpression(String definitionNamespace, String definitionName, String content) {
        super(mock(ASTNode.class));

        this.definitionNamespace = definitionNamespace;
        this.definitionName = definitionName;
        this.content = content;
    }

    public MockJSAssignmentExpression(JSDefinitionExpression definition, String content) {
        super(mock(ASTNode.class));

        this.definition = definition;
        this.content = content;
    }

    @Override
    public JSExpression[] getChildren()
    {
        if(definition == null)
        {
            return new JSExpression[] { new MockJSDefinitionExpression(definitionNamespace, definitionName), BasicPsiElements.expressionFromText(content) };
        }
        else
        {
            return new JSExpression[] { this.definition, BasicPsiElements.expressionFromText(content)};
        }
    }
}
