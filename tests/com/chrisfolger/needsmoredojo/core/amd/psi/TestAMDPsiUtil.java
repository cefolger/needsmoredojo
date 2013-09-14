package com.chrisfolger.needsmoredojo.core.amd.psi;

import com.chrisfolger.needsmoredojo.core.amd.importing.UnusedImportsRemover;
import com.chrisfolger.needsmoredojo.testutil.BasicPsiElements;
import com.chrisfolger.needsmoredojo.testutil.MockJSElementInterface;
import com.chrisfolger.needsmoredojo.testutil.MockJSLiteralExpression;
import com.chrisfolger.needsmoredojo.testutil.MockPsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestAMDPsiUtil
{
    private Set<PsiElement> removeList;

    @Before
    public void setup()
    {
        removeList = new HashSet<PsiElement>();
    }

    @Test
    public void deleteDefineLiteral_normalCommentIsNotRemoved()
    {
        MockPsiComment comment = new MockPsiComment("/*foo*/");
        MockJSLiteralExpression literal = new MockJSLiteralExpression("dijit/layout/ContentPane");

        BasicPsiElements.createChain(new MockJSElementInterface[]{
                literal,
                comment,
                BasicPsiElements.comma()});

        AMDPsiUtil.removeDefineLiteral(literal, removeList);

        assertFalse(removeList.contains(comment));
    }

    @Test
    public void deleteDefineLiteral_ignoreCommentIsRemoved()
    {
        MockPsiComment comment = new MockPsiComment(UnusedImportsRemover.IGNORE_COMMENT);
        MockJSLiteralExpression literal = new MockJSLiteralExpression("dijit/layout/ContentPane");

        BasicPsiElements.createChain(new MockJSElementInterface[]{
                literal,
                comment,
                BasicPsiElements.comma()});

        AMDPsiUtil.removeDefineLiteral(literal, removeList);

        assertTrue(removeList.contains(comment));
    }
}
