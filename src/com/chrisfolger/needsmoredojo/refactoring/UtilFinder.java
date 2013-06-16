package com.chrisfolger.needsmoredojo.refactoring;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSRecursiveElementVisitor;
import com.intellij.lang.javascript.psi.JSReturnStatement;

public class UtilFinder
{
    public JSRecursiveElementVisitor getReturnCallbackVisitor(final DeclareFinder.CompletionCallback onReturnFound)
    {
        return new JSRecursiveElementVisitor() {
            public void visitJSReturnStatement(JSReturnStatement statement)
            {

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
                function.acceptChildren(getReturnCallbackVisitor(new ClassConverter()));

                return;
            }
        };
    }
}
