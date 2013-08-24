package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.util.DefineStatement;
import com.chrisfolger.needsmoredojo.testutil.*;
import com.intellij.lang.javascript.psi.JSElement;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class TestAMDImportLocator
{
    private AMDImportLocator locator;

    private MockJSArrayLiteralExpression literal;
    private MockJSFunctionExpression function;
    private DefineStatement defineStatement;

    @Before
    public void setup()
    {
        locator = new AMDImportLocator();

        literal = new MockJSArrayLiteralExpression(new String[] { "a/b/a", "a/b/b", "a/b/c"});
        function = new MockJSFunctionExpression(new String[] { "a", "b", "c"});
        defineStatement = new DefineStatement(literal, function, "irrelevant");
    }

    @Test
    /**
     * covers the case:  (underscore = cursor)
     *
     * define(['a/b/a_', 'a/b/b', 'a/b/c'], function(a, b, c){});
     *
     */
    public void testCursorInsideLiteral()
    {
        MockJSElement caretElement = new MockJSElement("a/b/a");
        caretElement.setParent(new MockJSLiteralExpression("a/b/a"));

        JSElement defineLiteral = locator.getDefineLiteral(caretElement, defineStatement);

        assertNotNull(defineLiteral);
        assertEquals("a/b/a", defineLiteral.getText());
    }

    @Test
    /**
     * covers the case:  (underscore = cursor)
     *
     * define(['a/b/a'_, 'a/b/b', 'a/b/c'], function(a, b, c){});
     *
     */
    public void testCursorBeforeComma()
    {
        MockJSElement caretElement = BasicPsiElements.comma();
        caretElement.setPrevSibling(new MockJSLiteralExpression("a/b/a"));

        JSElement defineLiteral = locator.getDefineLiteral(caretElement, defineStatement);

        assertNotNull(defineLiteral);
        assertEquals("a/b/a", defineLiteral.getText());
    }

    @Test
    /**
     * covers the case:  (underscore = cursor)
     *
     * define(['a/b/a', 'a/b/b', 'a/b/c'], function(a_, b, c){});
     *
     */
    public void testCursorBeforeCommaForParameter()
    {
        MockJSElement caretElement = BasicPsiElements.comma();
        caretElement.setPrevSibling(new MockJSParameter("a"));

        JSElement parameter = locator.getParameter(caretElement, defineStatement);

        assertNotNull(parameter);
        assertEquals("a", parameter.getText());
    }

    @Test
    /**
     * covers the case:  (underscore = cursor)
     *
     * define(['a/b/a', 'a/b/b', 'a/b/c'], function(_a, b, c){});
     *
     */
    public void testCursorBeforeParameter()
    {
        MockJSElement caretElement = new MockJSElement("a");
        caretElement.setParent(new MockJSParameter("a"));

        JSElement parameter = locator.getParameter(caretElement, defineStatement);

        assertNotNull(parameter);
        assertEquals("a", parameter.getText());
    }

    @Test
    /**
     * covers the case:  (underscore = cursor)
     *
     * define(['a/b/a', 'a/b/b', 'a/b/c'], function(_a, b, c){});
     *
     */
    public void testDefineLiteralIsRetrievedWhenCursorIsOverParameter()
    {
        MockJSElement caretElement = new MockJSElement("a");
        caretElement.setParent(new MockJSParameter("a"));

        JSElement defineLiteral = locator.getDefineLiteral(caretElement, defineStatement);

        assertNotNull(defineLiteral);
        assertEquals("a/b/a", defineLiteral.getText());
    }

    @Test
    /**
     * covers the case:  (underscore = cursor)
     *
     * define(['a/b/a_', 'a/b/b', 'a/b/c'], function(a, b, c){});
     *
     */
    public void testParameterIsRetrievedWhenCursorIsOverDefineLiteral()
    {
        MockJSElement caretElement = BasicPsiElements.comma();
        caretElement.setPrevSibling(new MockJSLiteralExpression("a/b/a"));

        JSElement parameter = locator.getParameter(caretElement, defineStatement);

        assertNotNull(parameter);
        assertEquals("a", parameter.getText());
    }
}
