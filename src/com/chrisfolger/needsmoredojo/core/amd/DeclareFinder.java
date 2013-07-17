package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.refactoring.ClassToUtilConverter;
import com.chrisfolger.needsmoredojo.core.util.DeclareUtil;
import com.chrisfolger.needsmoredojo.core.util.DefineUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiFile;

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
                DefineUtil.DefineStatementItems items = new DefineUtil().getDefineStatementItemsFromArguments(element.getArguments());
                onDefineFound.run(new Object[] { element, items.getFunction().getFunction()});

                return;
            }
        };
    }
}
