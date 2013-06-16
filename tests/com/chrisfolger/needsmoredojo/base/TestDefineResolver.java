package com.chrisfolger.needsmoredojo.base;

import com.chrisfolger.needsmoredojo.testutil.MockJSFunction;
import com.chrisfolger.needsmoredojo.testutil.MockJSFunctionExpression;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
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
    public void foo()
    {
        JSRecursiveElementVisitor visitor = resolver.getDefineAndParametersVisitor(defines, parameters);
        visitor.visitJSCallExpression(getDojoAMDImportStatement(new String[] {"'dojo/_base/array'"}, new String[] { "array" }));

        assertEquals("'dojo/_base/array'", parameters.get(0).getText());
    }

    private JSCallExpression getDojoAMDImportStatement(String[] defines, String[] parameters)
    {
        JSCallExpression callExpression = mock(JSCallExpression.class);
        JSExpression[] arguments = new JSExpression[2];

        // set up the first argument in the dojo amd import call
        JSArrayLiteralExpression defineExpression = mock(JSArrayLiteralExpression.class);
        JSExpression[] defineExpressions = new JSExpression[defines.length];
        when(defineExpression.getExpressions()).thenReturn(defineExpressions);
        arguments[0] = defineExpression;
        for(int i=0;i<defines.length;i++)
        {
            defineExpressions[i] = mock(JSExpression.class);
            when(defineExpressions[i].getText()).thenReturn(defines[i]);
        }

        // set up the second argument which is the function call
        JSFunctionExpression functionExpression = new MockJSFunctionExpression(parameters);
        arguments[1] = functionExpression;

        when(callExpression.getArguments()).thenReturn(arguments);
        return callExpression;
    }
}
