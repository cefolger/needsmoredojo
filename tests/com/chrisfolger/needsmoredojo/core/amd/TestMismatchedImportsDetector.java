
package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.amd.MismatchedImportsDetector;
import com.intellij.psi.PsiElement;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestMismatchedImportsDetector
{
    private MismatchedImportsDetector detector;
    private Map<String, String> exceptions;

    @Before
    public void setup()
    {
        detector = new MismatchedImportsDetector();
        exceptions = new HashMap<String, String>();
    }

    @Test
    public void simpleCorrectList()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/on"), createPsiElement("dojo/_base/array"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(0, detector.matchOnList(defines, parameters, exceptions).size());
    }

    @Test
    public void simpleMismatchList()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/on"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(2, detector.matchOnList(defines, parameters, exceptions).size());
    }

    @Test
    public void swappedOrderMismatch()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/_base/array"), createPsiElement("dojo/on"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(2, detector.matchOnList(defines, parameters, exceptions).size());
    }

    private PsiElement createPsiElement(String text)
    {
        PsiElement result = mock(PsiElement.class);
        when(result.getText()).thenReturn(text);

        return result;
    }
}
