package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.testutil.BasicPsiElements;
import com.chrisfolger.needsmoredojo.testutil.MockJSElement;
import com.chrisfolger.needsmoredojo.testutil.MockJSElementInterface;
import com.chrisfolger.needsmoredojo.testutil.MockJSLiteralExpression;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestImportReorderer
{
    private ImportReorderer reorderer;
    private MockJSElementInterface[] first;
    private MockJSElementInterface[] second;

    @Before
    public void setup()
    {
        reorderer = new ImportReorderer();
        first = new MockJSElementInterface[] {
                new MockJSLiteralExpression("'first'"), BasicPsiElements.comma(), new MockJSElement("\n")
        };

        second = new MockJSElementInterface[] {
                new MockJSLiteralExpression("'second'"), BasicPsiElements.comma(), new MockJSElement("\n")
        };

        BasicPsiElements.createChain((MockJSElementInterface[]) ArrayUtils.addAll(first, second));
    }

    @Test
    /**
     * test this case:
     *
     * "abc"_,
     */
    public void testFindingSourceFromComma()
    {
        PsiElement[] results = reorderer.getSourceAndDestination(second[1], ImportReorderer.Direction.UP);

        assertEquals(second[0], results[0]);
        assertEquals(first[0], results[1]);
    }

    @Test
    /**
     * test this case:
     *
     * "abc"\n_,
     */
    public void testFindingSourceFromLineEnding()
    {
        PsiElement[] results = reorderer.getSourceAndDestination(second[2], ImportReorderer.Direction.UP);

        assertEquals(second[0], results[0]);
        assertEquals(first[0], results[1]);
    }

    @Test
    /**
     * test this case:
     *
     * "abc_",
     */
    public void testFindingSourceFromInsideLiteral()
    {
        PsiElement[] results = reorderer.getSourceAndDestination(second[0], ImportReorderer.Direction.UP);

        assertEquals(second[0], results[0]);
        assertEquals(first[0], results[1]);
    }
}
