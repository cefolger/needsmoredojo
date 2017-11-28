package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.impl.JSElementImpl;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

import static org.mockito.Mockito.mock;

public class BasicPsiElements
{
    public static JSLiteralExpression Null()
    {
        return new MockJSLiteralExpression("null");
    }

    public static MockJSElementInterface defineFunction()
    {
        MockJSElement function = new MockJSElement("function()");
        MockJSElement paren = new MockJSElement(")");
        paren.isLastChildOf(function);

        return function;
    }

    public static MockJSArrayLiteralExpression define()
    {
        MockJSArrayLiteralExpression literal = new MockJSArrayLiteralExpression(new String[0]);
        MockJSElement bracket = new MockJSElement("]");
        literal.setLastChild(bracket);
        literal.setBracket(bracket);

        return literal;
    }

    public static JSElement elementFromText(final String text)
    {
        return new JSElementImpl(mock(IElementType.class)) {
            @Override
            public String getText()
            {
                return text;
            }
        };
    }

    public static void createChain(MockJSElementInterface[] elementsInSequence)
    {
        for(int i=0;i<elementsInSequence.length-1;i++)
        {
            elementsInSequence[i].comesBefore(elementsInSequence[i+1]);
        }
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
        return new JSExpressionImpl(mock(IElementType.class)) {
            @Override
            public String getText()
            {
                return text;
            }
        };
    }
}
