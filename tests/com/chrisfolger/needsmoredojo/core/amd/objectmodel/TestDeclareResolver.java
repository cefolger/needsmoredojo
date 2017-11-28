package com.chrisfolger.needsmoredojo.core.amd.objectmodel;

import com.chrisfolger.needsmoredojo.testutil.*;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDeclareResolver
{
    private DeclareResolver resolver;

    @Before
    public void setup()
    {
        resolver = new DeclareResolver();

        Application applicationMock = mock(Application.class);
        Disposable disposableMock = mock(Disposable.class);
        when(applicationMock.isUnitTestMode()).thenReturn(true);
        ApplicationManager.setApplication(applicationMock, disposableMock);
    }

    @Test
    public void testTheBasicHappyPath()
    {
        Map<String, String> propertyMap = new HashMap<String, String>();
        propertyMap.put("property 1", "value");

        JSExpression[] arguments = new JSExpression[] {
                new MockJSArrayLiteralExpression(new String[] { "mixin1", "mixin2"}),
                new MockJSObjectLiteralExpression(propertyMap)
        };

        JSCallExpression callExpression = new MockJSCallExpression(arguments);
        Object[] statements = new Object[] {callExpression, null};

        DeclareStatementItems result = resolver.getDeclareStatementFromParsedStatement(statements);
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

        DeclareStatementItems result = resolver.getDeclareStatementFromParsedStatement(statements);
        assertEquals(0, result.getExpressionsToMixin().length);
        assertEquals(1, result.getMethodsToConvert().length);
    }

    @Test
    public void classNameIsRetrieved()
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

        DeclareStatementItems result = resolver.getDeclareStatementFromParsedStatement(statements);
        assertEquals("test class", result.getClassName().getText());
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

        DeclareStatementItems result = resolver.getDeclareStatementFromParsedStatement(statements);
        assertEquals(2, result.getExpressionsToMixin().length);
        assertEquals(1, result.getMethodsToConvert().length);
    }

    @Test
    public void testWhenMixinArrayIsNull()
    {
        Map<String, String> propertyMap = new HashMap<String, String>();
        propertyMap.put("property 1", "value");

        JSExpression[] arguments = new JSExpression[] {
                new MockJSLiteralExpression("test class"),
                new MockJSLiteralExpression("null"),
                new MockJSObjectLiteralExpression(propertyMap)
        };

        JSCallExpression callExpression = new MockJSCallExpression(arguments);
        Object[] statements = new Object[] {callExpression, null};

        DeclareStatementItems result = resolver.getDeclareStatementFromParsedStatement(statements);
        assertNotNull(result);
    }
}
