package com.chrisfolger.needsmoredojo.core.amd.psi;

import com.chrisfolger.needsmoredojo.testutil.BasicPsiElements;
import com.chrisfolger.needsmoredojo.testutil.MockJSElementInterface;
import com.chrisfolger.needsmoredojo.testutil.MockJSLiteralExpression;
import com.chrisfolger.needsmoredojo.testutil.MockPsiComment;
import com.intellij.psi.tree.IElementType;
import org.junit.Test;

public class TestAMDPsiUtil
{
    @Test
    public void foo()
    {
        new MockPsiComment("foo");

        BasicPsiElements.createChain(new MockJSElementInterface[]{
                new MockJSLiteralExpression("dijit/layout/ContentPane"),

                BasicPsiElements.comma()});
    }
}
