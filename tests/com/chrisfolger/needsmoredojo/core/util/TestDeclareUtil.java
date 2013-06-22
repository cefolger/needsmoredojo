package com.chrisfolger.needsmoredojo.core.util;

import com.chrisfolger.needsmoredojo.testutil.*;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestDeclareUtil
{
    private DeclareUtil util;

    @Before
    public void setup()
    {
        util = new DeclareUtil();
    }

    @Test
    public void testTheBasicHappyBath()
    {
        Map<String, String> propertyMap = new HashMap<String, String>();
        propertyMap.put("property 1", "value");

        JSExpression[] arguments = new JSExpression[] {
                new MockJSArrayLiteralExpression(new String[] { "mixin1", "mixin2"}),
                new MockJSObjectLiteralExpression(propertyMap)
        };

        JSCallExpression callExpression = new MockJSCallExpression(arguments);
        Object[] statements = new Object[] {callExpression, null};

        DeclareUtil.DeclareStatementItems result = util.getDeclareStatementFromParsedStatement(statements);
        assertEquals(2, result.getExpressionsToMixin().length);
        assertEquals(1, result.getMethodsToConvert().length);
    }

    @Test
    public void testWhenFirstArgumentIsNull()
    {
        Map<String, String> propertyMap = new HashMap<String, String>();
        propertyMap.put("property 1", "value");

        JSExpression[] arguments = new JSExpression[] {
                BasicPsiElements.Null(),
                new MockJSObjectLiteralExpression(propertyMap)
        };

        JSCallExpression callExpression = new MockJSCallExpression(arguments);
        Object[] statements = new Object[] {callExpression, null};

        DeclareUtil.DeclareStatementItems result = util.getDeclareStatementFromParsedStatement(statements);
        assertEquals(0, result.getExpressionsToMixin().length);
        assertEquals(1, result.getMethodsToConvert().length);
    }

    @Test
    public void testWhenFirstArgumentIsAClassName()
    {
        Map<String, String> propertyMap = new HashMap<String, String>();
        propertyMap.put("property 1", "value");

        JSExpression[] arguments = new JSExpression[] {
                new MockJSLiteralExpression("test class"),
                new MockJSArrayLiteralExpression(new String[] { "define 1", "define 2"}),
                new MockJSObjectLiteralExpression(propertyMap)
        };

        JSCallExpression callExpression = new MockJSCallExpression(arguments);
        Object[] statements = new Object[] {callExpression, null};

        DeclareUtil.DeclareStatementItems result = util.getDeclareStatementFromParsedStatement(statements);
        assertEquals(2, result.getExpressionsToMixin().length);
        assertEquals(1, result.getMethodsToConvert().length);
    }
}
