package com.chrisfolger.needsmoredojo.core.amd.objectmodel;

import com.chrisfolger.needsmoredojo.core.amd.CompletionCallback;
import com.chrisfolger.needsmoredojo.core.util.DeclareUtil;
import com.intellij.lang.javascript.psi.*;

public class DeclareResolver
{
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
}
