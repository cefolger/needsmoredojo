package com.chrisfolger.needsmoredojo.core.amd.define;

import com.chrisfolger.needsmoredojo.core.amd.CompletionCallback;
import com.chrisfolger.needsmoredojo.core.amd.importing.InvalidDefineException;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 Parse the AMD imports in a dojo module according to this syntax:

 define([modules], function(modules) {});

 */
public class DefineResolver
{
    private Logger logger = Logger.getLogger(DefineResolver.class);

    public void addDefinesAndParametersOfImportBlock(JSCallExpression existingImportBlock, final List<PsiElement> defines, final List<PsiElement> parameters) throws InvalidDefineException
    {
        JSExpression[] arguments = existingImportBlock.getArguments();

        DefineStatement items = getDefineStatementItemsFromArguments(arguments, existingImportBlock);
        if(items == null)
        {
            throw new InvalidDefineException();
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

    /**
     * @deprecated use gatherDefineAndParameters instead.
     * @param defines
     * @param parameters
     * @param defineVisitor
     * @return
     */
    protected JSRecursiveElementVisitor getDefineAndParametersVisitor( final List<PsiElement> defines, final List<PsiElement> parameters, final PsiElementVisitor defineVisitor)
    {
        return new JSRecursiveElementVisitor() {
            @Override
            public void visitJSCallExpression(JSCallExpression element)
            {
                // if the user entered invalid syntax we don't want to account for every case, so just catch and log it
                try
                {
                    if(!element.getMethodExpression().getText().equals("define"))
                    {
                        return;
                    }

                    try
                    {
                        addDefinesAndParametersOfImportBlock(element, defines, parameters);
                    }
                    catch(InvalidDefineException exc)
                    {
                        super.visitJSCallExpression(element);
                        return;
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

    /**
     * @deprecated use gatherDefineAndParameters instead.
     * @param defines
     * @param parameters
     * @return
     */
    protected JSRecursiveElementVisitor getDefineAndParametersVisitor( final List<PsiElement> defines, final List<PsiElement> parameters)
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
                DefineStatement items = getDefineStatementItemsFromArguments(element.getArguments(), element);
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
    public @Nullable DefineStatement getDefineStatementItems(PsiFile file)
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
                items[0] = getDefineStatementItemsFromArguments(callExpression.getArguments(), callExpression);
            }
        }));

        if(items[0] == null)
        {
            return null;
        }

        return items[0];
    }

    /**
     * Searches for the nearest require/define block assuming the given element is a child of it.
     *
     * @param element
     * @return
     */
    public @Nullable DefineStatement getNearestImportBlock(PsiElement element)
    {
        PsiElement parent = element.getParent();
        while(parent != null)
        {
            if(parent instanceof JSCallExpression)
            {
                JSCallExpression statement = (JSCallExpression) parent;
                if(statement.getMethodExpression() != null && (statement.getMethodExpression().getText().equals("define")
                        || statement.getMethodExpression().getText().equals("require")))
                {
                    return getDefineStatementItemsFromArguments(statement.getArguments(), statement);
                }
            }

            parent = parent.getParent();
        }

        return null;
    }

    public DefineStatement getDefineStatementItemsFromArguments(JSExpression[] arguments, JSCallExpression original)
    {
        // account for when we get this (even though this is defined as legacy) :
        /**
         * define('classname', [], function(...){});
         */
        int argumentOffset = 0;
        String className = null;

        if(arguments.length > 1 && arguments[0] instanceof JSLiteralExpression && arguments[1] instanceof JSArrayLiteralExpression)
        {
            argumentOffset = 1;
            className = arguments[0].getText();
        }
        else if(!(arguments.length > 1 && arguments[0] instanceof JSArrayLiteralExpression && arguments[1] instanceof JSFunctionExpression))
        {
            return null;
        }

        // get the first argument which should be an array literal
        JSArrayLiteralExpression literalExpressions = (JSArrayLiteralExpression) arguments[0 + argumentOffset];

        // get the second argument which should be a function
        JSFunctionExpression function = (JSFunctionExpression) arguments[1 + argumentOffset];

        return new DefineStatement(literalExpressions, function, className, original);
    }

    /**
     * gets a set of all define and require blocks from a given file.
     *
     * @param file
     * @return
     */
    public Set<JSCallExpression> getAllImportBlocks(PsiFile file)
    {
        final Set<JSCallExpression> listOfDefinesOrRequiresToVisit = new LinkedHashSet<JSCallExpression>();
        JSRecursiveElementVisitor defineOrRequireVisitor = new JSRecursiveElementVisitor() {
            @Override
            public void visitJSCallExpression(JSCallExpression expression)
            {
                if(expression.getMethodExpression().getText().equals("define") || expression.getMethodExpression().getText().equals("require"))
                {
                    listOfDefinesOrRequiresToVisit.add(expression);
                }
                super.visitJSCallExpression(expression);
            }
        };

        file.accept(defineOrRequireVisitor);

        return listOfDefinesOrRequiresToVisit;
    }
}
