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

        file.acceptChildren(getDefineVisitor());
    }

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

    public JSRecursiveElementVisitor getDefineVisitor()
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
