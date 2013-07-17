package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.testutil.BasicPsiElements;
import com.chrisfolger.needsmoredojo.testutil.MockJSElement;
import com.intellij.psi.PsiElement;
import org.junit.Before;
import org.junit.Test;

public class TestImportReorderer
{
    private ImportReorderer reorderer;
    private PsiElement[] source;

    @Before
    public void setup()
    {
        reorderer = new ImportReorderer();
       // source = BasicPsiElements.createChain(new MockJSElement[] {  BasicPsiElements.comma() });
    }

    @Test
    /**
     * test this case:
     *
     * "abc"_,
     */
    public void testFindingSourceFromComma()
    {
        //reorderer.getSourceAndDestination();
    }
}
