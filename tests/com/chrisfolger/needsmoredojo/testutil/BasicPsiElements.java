package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.javascript.psi.JSLiteralExpression;

public class BasicPsiElements
{
    public static JSLiteralExpression Null()
    {
        return new MockJSLiteralExpression("null");
    }
}
