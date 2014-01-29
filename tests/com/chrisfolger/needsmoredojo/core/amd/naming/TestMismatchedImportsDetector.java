
package com.chrisfolger.needsmoredojo.core.amd.naming;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.psi.PsiElement;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestMismatchedImportsDetector
{
    private MismatchedImportsDetector detector;
    private Map<String, String> exceptions;
    private DojoSettings settings;

    @Before
    public void setup()
    {
        settings = mock(DojoSettings.class);
        detector = new MismatchedImportsDetector();
        exceptions = new HashMap<String, String>();
    }

    @Test
    public void simpleCorrectList()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/on"), createPsiElement("dojo/_base/array"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(0, detector.matchOnList(defines, parameters, exceptions, settings).size());
    }

    @Test
    public void simpleMismatchList()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/on"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(2, detector.matchOnList(defines, parameters, exceptions, settings).size());
    }

    @Test
    public void swappedOrderMismatch()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/_base/array"), createPsiElement("dojo/on"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(2, detector.matchOnList(defines, parameters, exceptions, settings).size());
    }

    private PsiElement createPsiElement(String text)
    {
        PsiElement result = mock(PsiElement.class);
        when(result.getText()).thenReturn(text);

        return result;
    }
}
