package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.refactoring.ClassToUtilConverter;
import com.chrisfolger.needsmoredojo.core.util.DeclareUtil;
import com.chrisfolger.needsmoredojo.core.util.DefineStatement;
import com.chrisfolger.needsmoredojo.core.util.DefineUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class DeclareFinder
{
    public interface CompletionCallback
    {
        public void run(Object[] result);
    }

    public void convertToUtilPattern(PsiFile file)
    {
        // steps:
        // get the return declare statement
        // get all of the literal expressions

        file.acceptChildren(getVisitorToConvertToUtilPattern());
    }

    /**
     * returns a visitor that will find a declare statement in a dojo module
     *
     * @param onReturnFound this is the callback that will run when the declare statement is found
     * @return the visitor
     */
    public JSRecursiveElementVisitor getVisitorToRetrieveDeclare(final CompletionCallback onReturnFound)
    {
        return new JSRecursiveElementVisitor() {
            @Override
            public void visitJSLocalVariable(JSLocalVariable variable)
            {
                if(variable.getInitializer() instanceof JSCallExpression && variable.getInitializer().getText().startsWith("declare"))
                {
                    onReturnFound.run(new Object[] { variable.getInitializer(), variable});
                    return;
                }

                super.visitJSLocalVariable(variable);
            }

            @Override
            public void visitJSReturnStatement(JSReturnStatement statement)
            {
                if(statement.getChildren().length == 0)
                {
                    super.visitJSReturnStatement(statement);
                    return;
                }

                if(!(statement.getChildren()[0] instanceof JSCallExpression))
                {
                    super.visitJSReturnStatement(statement);
                    return;
                }

                JSCallExpression expression = (JSCallExpression) statement.getChildren()[0];
                if(DeclareUtil.isDeclareFunction(expression.getMethodExpression()))
                {
                    onReturnFound.run(new Object[] { expression, statement});
                    return;
                }

                super.visitJSReturnStatement(statement);
            }
        };
    }

    /**
     * this returns a visitor that will search for a define statement, and on finding it will then
     * search for the declare statement in a dojo module
     *
     * @param onDeclareFound this is the callback that will be executed when the declare statement is found
     * @return the visitor
     */
    public JSRecursiveElementVisitor getDefineVisitorToRetrieveDeclareObject(final CompletionCallback onDeclareFound)
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
                JSFunction function = (JSFunction) element.getArguments()[1];
                function.acceptChildren(getVisitorToRetrieveDeclare(onDeclareFound));

                return;
            }
        };
    }

    /**
     * This method returns a visitor that will take a dojo module and convert it to use a util pattern
     * instead of a class pattern
     *
     * @return the visitor
     */
    public JSRecursiveElementVisitor getVisitorToConvertToUtilPattern()
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
                JSFunction function = (JSFunction) element.getArguments()[1];
                function.acceptChildren(getVisitorToRetrieveDeclare(new ClassToUtilConverter()));

                return;
            }
        };
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
