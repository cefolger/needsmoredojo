package com.chrisfolger.needsmoredojo.core.refactoring;

import com.chrisfolger.needsmoredojo.testutil.MockJSAssignmentExpression;
import com.chrisfolger.needsmoredojo.testutil.MockJSDefinitionExpressionWithIndexedProperty;
import com.chrisfolger.needsmoredojo.testutil.MockJSExpressionStatement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TestUtilToClassConverter
{
    private UtilToClassConverter converter;

    @Before
    public void setup()
    {
        converter = new UtilToClassConverter();
    }

    @Test
    public void testPropertyIsGenerated()
    {
        List<JSExpressionStatement> methods = new ArrayList<JSExpressionStatement>();
        methods.add(new MockJSExpressionStatement(new MockJSAssignmentExpression("util.", "property", "'value'")));

        String result = converter.buildUtilPatternString(null, new JSExpression[0], methods, "util");
        assertTrue(result.contains("property: 'value'"));
    }

    @Test
    public void indexPropertyIsConverted()
    {
        List<JSExpressionStatement> methods = new ArrayList<JSExpressionStatement>();
        methods.add(new MockJSExpressionStatement(new MockJSAssignmentExpression(new MockJSDefinitionExpressionWithIndexedProperty("util", "'-test-'"), "value")));

        String result = converter.buildUtilPatternString(null, new JSExpression[0], methods, "util");
        assertTrue(result.contains("-test-': value"));
    }
}
