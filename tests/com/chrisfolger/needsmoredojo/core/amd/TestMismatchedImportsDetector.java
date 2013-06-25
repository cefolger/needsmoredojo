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
    public void matchOnSimpleCase()
    {
        assertTrue(detector.defineMatchesParameter("dijit/layout/BorderContainer", "BorderContainer", exceptions));
    }

    @Test
    public void mismatchOnSimpleCase()
    {
        assertFalse(detector.defineMatchesParameter("dijit/layout/ContentPane", "BorderContainer", exceptions));
    }

    @Test
    public void matchOnTemplate()
    {
        assertTrue(detector.defineMatchesParameter("dojo/text!foo/template.html", "template", exceptions));
    }

    @Test
    public void matchOnTemplateWithTemplatesFolder()
    {
        assertTrue(detector.defineMatchesParameter("dojo/text!./templates/MainToolbar.html", "template", exceptions));
    }

    @Test
    public void matchOnTemplateWithRelativePath()
    {
        assertTrue(detector.defineMatchesParameter("dojo/text!./Foo.html", "template", exceptions));
    }

    @Test
    public void matchOnTemplateWithExplicitName()
    {
        assertTrue(detector.defineMatchesParameter("dojo/text!./Foo.html", "fooTemplate", exceptions));
    }

    @Test
    public void matchI18n()
    {
        assertTrue(detector.defineMatchesParameter("dojo/i18n!./resources/resources", "resources", exceptions));
    }

    @Test
    public void matchI18nWithConvention()
    {
        // I've seen all of these in the dojo libraries
        assertTrue(detector.defineMatchesParameter("dojo/i18n!./MainToolbar/resources", "resources", exceptions));
        assertTrue(detector.defineMatchesParameter("dojo/i18n!./MainToolbar/resources", "nlsMaintoolbar", exceptions));
        assertTrue(detector.defineMatchesParameter("dojo/i18n!./MainToolbar/resources", "i18nMaintoolbar", exceptions));
        assertTrue(detector.defineMatchesParameter("dojo/i18n!./nls/buttons", "_nls", exceptions));
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

    @Test
    // we encourage sticking to the convention of using the filename as the parameter variable name
    public void typingMismatch()
    {
        assertFalse(detector.defineMatchesParameter("dijit/layout/ContentPane", "pane", exceptions));
    }

    @Test
    // we encourage the x-y -> xY convention because that's what's used in the dojo reference examples
    public void domClassesMatch()
    {
        assertTrue(detector.defineMatchesParameter("dojo/dom-construct", "domConstruct", exceptions));
    }

    // TODO performance penalty when we hit about 1000 lines

    @Test
    /**
     * due to presence of _ prefixed modules such as _WidgetBase, we have to account for this
     */
    public void testDefinesWithUnderscores()
    {
        assertTrue(detector.defineMatchesParameter("dijit/_WidgetBase", "WidgetBase", exceptions));
    }

    @Test
    /**
     * there is a bug where if you have a define without slashes it will cause a false positive
     */
    public void testNoSlashMatchesCorrectly()
    {
        assertTrue(detector.defineMatchesParameter("a", "a", exceptions));
    }

    @Test
    public void testDoubleQuotes()
    {
        assertTrue(detector.defineMatchesParameter("\"a\"", "a", exceptions));
    }

    @Test
    public void relativePathsMismatch()
    {
        assertFalse(detector.defineMatchesParameter("\"../../../../foo/bar/Element\"", "Eledment", exceptions));
    }

    @Test
    public void baseFxException()
    {
        assertTrue(detector.defineMatchesParameter("dojo/_base/fx", "baseFx", exceptions));
        assertTrue(detector.defineMatchesParameter("dojo/_base/fx", "fx", exceptions));
    }

    @Test
    public void testException()
    {
        // has is an explicit exception
        exceptions.put("dojo/sniff", "has");
        assertTrue(detector.defineMatchesParameter("dojo/sniff", "has", exceptions));
    }

    private PsiElement createPsiElement(String text)
    {
        PsiElement result = mock(PsiElement.class);
        when(result.getText()).thenReturn(text);

        return result;
    }

    @Test
    public void testI18nModule()
    {
        assertFalse(detector.defineMatchesParameter("dojo/i18n", "resources", exceptions));
    }
}
