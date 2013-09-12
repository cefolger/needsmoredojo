package com.chrisfolger.needsmoredojo.core.amd.objectmodel;

import com.chrisfolger.needsmoredojo.core.amd.CompletionCallback;
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
                if(AMDValidator.isDeclareFunction(expression.getMethodExpression()))
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

    public DeclareStatementItems getDeclareStatementFromParsedStatement(Object[] result)
    {
        return getDeclareStatementFromParsedStatement(result, true);
    }

    public DeclareStatementItems getDeclareStatementFromParsedStatement(Object[] result, boolean parseMethodsFromObjectLiteral)
    {
        JSCallExpression expression = (JSCallExpression) result[0];

        // this will be used to determine what we mixin to the util
        JSExpression[] expressionsToMixin = new JSExpression[0];

        /*
            so many different possibilities...

            declare(null, {});
            declare([], {});
            declare(string, [], {});
            declare(string, mixin, {});
            declare(mixin, {});

            dojo.declare(...) (legacy)
         */
        int objectLiteralIndex = 1;
        if(expression.getArguments()[0] instanceof JSArrayLiteralExpression)
        {
            JSArrayLiteralExpression arrayLiteral = (JSArrayLiteralExpression) expression.getArguments()[0];
            expressionsToMixin = arrayLiteral.getExpressions();
        }
        else if (expression.getArguments()[0] instanceof JSReferenceExpression)
        {
            expressionsToMixin = new JSExpression[] { expression.getArguments()[0] };
        }
        else if (expression.getArguments().length == 3 && expression.getArguments()[1] instanceof JSReferenceExpression)
        {
            expressionsToMixin = new JSExpression[] { expression.getArguments()[1] };
            objectLiteralIndex = 2;
        }
        else if (expression.getArguments().length == 3 && expression.getArguments()[1] instanceof JSArrayLiteralExpression)
        {
            JSArrayLiteralExpression arrayLiteral = (JSArrayLiteralExpression) expression.getArguments()[1];
            expressionsToMixin = arrayLiteral.getExpressions();
            objectLiteralIndex = 2;
        }

        JSLiteralExpression className = null;
        if(expression.getArguments()[0] instanceof JSLiteralExpression)
        {
            className = (JSLiteralExpression) expression.getArguments()[0];
        }

        // now we need to get the object literal with all of the function names
        JSObjectLiteralExpression literal = (JSObjectLiteralExpression) expression.getArguments()[objectLiteralIndex];
        JSProperty[] methodsToConvert = literal.getProperties();

        return new DeclareStatementItems(className, expressionsToMixin, methodsToConvert, (JSElement) result[1]);
    }
}
