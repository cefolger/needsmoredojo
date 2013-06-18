package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.chrisfolger.needsmoredojo.testutil.MockJSArrayLiteralExpression;
import com.chrisfolger.needsmoredojo.testutil.MockJSFunctionExpression;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDefineResolver
{
    private DefineResolver resolver;
    private List<PsiElement> defines;
    private List<PsiElement> parameters;

    @Before
    public void setup()
    {
        resolver = new DefineResolver();
        defines = new ArrayList<PsiElement>();
        parameters = new ArrayList<PsiElement>();
    }

    @Test
    public void testParametersAndDefinesAreParsedCorrectly()
    {
        JSRecursiveElementVisitor visitor = resolver.getDefineAndParametersVisitor(defines, parameters);
        visitor.visitJSCallExpression(getDojoAMDImportStatement(new String[] {"'dojo/_base/array'"}, new String[] { "array" }));
        visitor.visitJSCallExpression(getDojoAMDImportStatement(new String[] {"'dijit/foo'"}, new String[] { "foo" }));

        assertEquals("'dojo/_base/array'", defines.get(0).getText());
    }

    private JSCallExpression getDojoAMDImportStatement(String[] defines, String[] parameters)
    {
        JSCallExpression callExpression = mock(JSCallExpression.class);
        JSExpression[] arguments = new JSExpression[2];

        // set up the first argument in the dojo amd import call
        JSArrayLiteralExpression defineExpression = new MockJSArrayLiteralExpression(defines);
        arguments[0] = defineExpression;

        // set up the second argument which is the function call
        JSFunctionExpression functionExpression = new MockJSFunctionExpression(parameters);
        arguments[1] = functionExpression;

        when(callExpression.getArguments()).thenReturn(arguments);
        return callExpression;
    }
}
