package com.chrisfolger.needsmoredojo.core.amd.define;

import com.chrisfolger.needsmoredojo.core.amd.CompletionCallback;
import com.chrisfolger.needsmoredojo.core.util.DefineStatement;
import com.chrisfolger.needsmoredojo.core.util.DefineUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 Parse the AMD imports in a dojo module according to this syntax:

 define([modules], function(modules) {});

 */
public class DefineResolver
{
    private Logger logger = Logger.getLogger(DefineResolver.class);

    public JSRecursiveElementVisitor getDefineAndParametersVisitor( final List<PsiElement> defines, final List<PsiElement> parameters, final PsiElementVisitor defineVisitor)
    {
        return new JSRecursiveElementVisitor() {
            @Override
            public void visitJSCallExpression(JSCallExpression element)
            {
                // if the user entered invalid syntax we don't want to account for every case, so just catch and log it
                try
                {
                    JSExpression[] arguments = element.getArguments();
                    if(!element.getMethodExpression().getText().equals("define"))
                    {
                        return;
                    }

                    DefineStatement items = new DefineUtil().getDefineStatementItemsFromArguments(arguments);
                    if(items == null)
                    {
                        super.visitJSCallExpression(element);
                        return;
                    }

                    // get the first argument which should be an array literal
                    JSArrayLiteralExpression literalExpressions = items.getArguments();
                    for(JSExpression expression : literalExpressions.getExpressions())
                    {
                        if(expression instanceof JSLiteralExpression)
                        {
                            JSLiteralExpression literal = (JSLiteralExpression) expression;
                            defines.add(literal);
                        }
                    }

                    // get the second argument which should be a function
                    JSFunctionExpression function = items.getFunction();
                    for(JSParameter parameter : function.getFunction().getParameters())
                    {
                        parameters.add(parameter);
                    }
                }
                catch(Exception e)
                {
                    logger.log(Priority.INFO, "exception ecountered in DefineResolver ", e);
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

    /**
     * Returns a visitor that will search a dojo module for its define statement, and execute a callback
     * when it finds it
     *
     * @deprecated use getDefineStatementItems instead.
     * @param onDefineFound the callback
     * @return the visitor
     */
    public JSRecursiveElementVisitor getDefineVisitor(final CompletionCallback onDefineFound)
    {
        return new JSRecursiveElementVisitor() {
            @Override
            public void visitJSCallExpression(JSCallExpression element)
            {
                if(!element.getMethodExpression().getText().equals("define"))
                {
                    super.visitJSCallExpression(element);
                    return;
                }

                // get the function
                DefineStatement items = new DefineUtil().getDefineStatementItemsFromArguments(element.getArguments());
                if(items == null)
                {
                    onDefineFound.run(null);
                    return;
                }

                onDefineFound.run(new Object[] { element, items.getFunction().getFunction()});
            }
        };
    }

    /**
     * Wrapper around the deprecated method getDefineVisitor. Using 'callbacks' was possibly the worst decision ever,
     * but a lot of the code uses this class. So this method wraps up the nasty callback syntax.
     *
     * @param file the PsiFile to retrieve the define statement from
     * @return the define statement and its items
     */
    public @Nullable
    DefineStatement getDefineStatementItems(PsiFile file)
    {
        final DefineStatement[] items = new DefineStatement[1];

        file.acceptChildren(getDefineVisitor(new CompletionCallback() {
            @Override
            public void run(Object[] result) {
                if(result == null || result[0] == null)
                {
                    items[0] = null;
                    return;
                }

                JSCallExpression callExpression = (JSCallExpression) result[0];
                items[0] = new DefineUtil().getDefineStatementItemsFromArguments(callExpression.getArguments());
            }
        }));

        if(items[0] == null)
        {
            return null;
        }

        return items[0];
    }
}
