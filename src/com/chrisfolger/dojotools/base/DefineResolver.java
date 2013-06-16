package com.chrisfolger.dojotools.base;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSCallExpressionImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 Parse the AMD imports in a dojo module according to this syntax:

 define([modules], function(modules) {});

 */
public class DefineResolver
{
    public JSRecursiveElementVisitor getDefineAndParametersVisitor( final List<PsiElement> defines, final List<PsiElement> parameters, final PsiElementVisitor defineVisitor)
    {
        return new JSRecursiveElementVisitor() {
            @Override
            public void visitJSCallExpression(JSCallExpression element)
            {
                JSExpression[] arguments = element.getArguments();

                if(!(arguments.length > 1 && arguments[0] instanceof JSArrayLiteralExpression && arguments[1] instanceof JSFunctionExpression))
                {
                    super.visitJSCallExpression(element);
                    return;
                }

                // get the first argument which should be an array literal
                JSArrayLiteralExpression literalExpressions = (JSArrayLiteralExpression) arguments[0];
                for(JSExpression expression : literalExpressions.getExpressions())
                {
                    if(expression instanceof JSLiteralExpression)
                    {
                        JSLiteralExpression literal = (JSLiteralExpression) expression;
                        defines.add(literal);
                    }
                }

                // get the second argument which should be a function
                JSFunctionExpression function = (JSFunctionExpression) arguments[1];
                for(JSParameter parameter : function.getFunction().getParameters())
                {
                    parameters.add(parameter);
                }

                super.visitJSCallExpression(element);
            }
        };
    }

    public JSRecursiveElementVisitor getDefineAndParametersVisitor( final List<PsiElement> defines, final List<PsiElement> parameters)
    {
        return getDefineAndParametersVisitor(defines, parameters, null);
    }

    public void gatherDefineAndParameters(PsiFile psiFile, final List<PsiElement> defines, final List<PsiElement> parameters)
    {
        psiFile.accept(getDefineAndParametersVisitor(defines, parameters));
    }
}
