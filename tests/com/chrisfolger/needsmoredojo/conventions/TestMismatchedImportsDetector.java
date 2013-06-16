package com.chrisfolger.needsmoredojo.conventions;

import com.intellij.psi.PsiElement;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Chris
 * Date: 1/7/13
 * Time: 9:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestMismatchedImportsDetector
{
    private MismatchedImportsDetector detector;

    @Before
    public void setup()
    {
        detector = new MismatchedImportsDetector();
    }

    @Test
    public void matchOnSimpleCase()
    {
        assertTrue(detector.defineMatchesParameter("dijit/layout/BorderContainer", "BorderContainer"));
    }

    @Test
    public void mismatchOnSimpleCase()
    {
        assertFalse(detector.defineMatchesParameter("dijit/layout/ContentPane", "BorderContainer"));
    }

    @Test
    public void matchOnTemplate()
    {
        assertTrue(detector.defineMatchesParameter("dojo/text!foo/template.html", "template"));
    }

    @Test
    public void simpleCorrectList()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/on"), createPsiElement("dojo/_base/array"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(0, detector.matchOnList(defines, parameters).size());
    }

    @Test
    public void simpleMismatchList()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/on"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(2, detector.matchOnList(defines, parameters).size());
    }

    @Test
    public void swappedOrderMismatch()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/_base/array"), createPsiElement("dojo/on"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(2, detector.matchOnList(defines, parameters).size());
    }

    @Test
    // we encourage sticking to the convention of using the filename as the parameter variable name
    public void typingMismatch()
    {
        assertFalse(detector.defineMatchesParameter("dijit/layout/ContentPane", "pane"));
    }

    @Test
    // we encourage the x-y -> xY convention because that's what's used in the dojo reference examples
    public void domClassesMatch()
    {
        assertTrue(detector.defineMatchesParameter("dojo/dom-construct", "domConstruct"));
    }

    // TODO performance penalty when we hit about 1000 lines

    @Test
    /**
     * due to presence of _ prefixed modules such as _WidgetBase, we have to account for this
     */
    public void testDefinesWithUnderscores()
    {
        assertTrue(detector.defineMatchesParameter("dijit/_WidgetBase", "WidgetBase"));
    }

    @Test
    /**
     * there is a bug where if you have a define without slashes it will cause a false positive
     */
    public void testNoSlashMatchesCorrectly()
    {
        assertTrue(detector.defineMatchesParameter("a", "a"));
    }

    @Test
    public void testDoubleQuotes()
    {
        assertTrue(detector.defineMatchesParameter("\"a\"", "a"));
    }

    @Test
    public void relativePathsMismatch()
    {
        assertFalse(detector.defineMatchesParameter("\"../../../../foo/bar/Element\"", "Eledment"));
    }

    private PsiElement createPsiElement(String text)
    {
        PsiElement result = mock(PsiElement.class);
        when(result.getText()).thenReturn(text);

        return result;
    }
}
