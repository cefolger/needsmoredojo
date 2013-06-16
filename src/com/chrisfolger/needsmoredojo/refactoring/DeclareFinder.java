package com.chrisfolger.needsmoredojo.refactoring;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSRecursiveElementVisitor;
import com.intellij.lang.javascript.psi.JSReturnStatement;
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

    public JSRecursiveElementVisitor getDefineCallbackVisitor(final CompletionCallback onReturnFound)
    {
        // TODO account for the case where the define passes a class name as the first argument
        return new JSRecursiveElementVisitor() {
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
                if(expression.getMethodExpression().getText().equals("declare"))
                {
                    onReturnFound.run(new Object[] { expression, statement});
                    return;
                }

                super.visitJSReturnStatement(statement);
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
                function.acceptChildren(getDefineCallbackVisitor(new UtilConverter()));

                return;
            }
        };
    }
}
