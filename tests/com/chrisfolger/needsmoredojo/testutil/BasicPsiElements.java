package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.impl.JSElementImpl;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.psi.PsiElement;

import static org.mockito.Mockito.mock;

public class BasicPsiElements
{
    public static JSLiteralExpression Null()
    {
        return new MockJSLiteralExpression("null");
    }

    public static JSElement elementFromText(final String text)
    {
        return new JSElementImpl(mock(ASTNode.class)) {
            @Override
            public String getText()
            {
                return text;
            }
        };
    }

    public static MockJSElement comma()
    {
        return new MockJSElement(",");
    }

    public static MockJSElement space()
    {
        return new MockJSElement(" ");
    }

    public static MockJSElement lineBreak()
    {
        return new MockJSElement("\n");
    }

    public static JSExpression expressionFromText(final String text)
    {
        return new JSExpressionImpl(mock(ASTNode.class)) {
            @Override
            public String getText()
            {
                return text;
            }
        };
    }
}
