package com.chrisfolger.needsmoredojo.refactoring;

import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiFile;

public class UtilFinder
{
    public JSElementVisitor getReturnCallbackVisitor(final DeclareFinder.CompletionCallback onReturnFound)
    {
        return new JSElementVisitor() {
            @Override
            public void visitJSBlock(JSBlockStatement element)
            {
                JSReturnStatement returnStatement = null;
                JSCallExpression declaration = null;

                for(JSStatement statement : element.getStatements())
                {
                    if(statement instanceof JSReturnStatement)
                    {
                        returnStatement = (JSReturnStatement) statement;
                    }
                    else if (statement instanceof JSVarStatement && statement.getText().contains("declare("))
                    {
                        for(JSVariable variable : ((JSVarStatement)statement).getVariables())
                        {
                            if(variable.getInitializerText().contains("declare"))
                            {
                                JSCallExpression declareCall = (JSCallExpression) variable.getInitializer();
                                declaration = declareCall;
                                break;
                            }
                        }
                    }

                    if(returnStatement != null && declaration != null)
                    {
                        onReturnFound.run(new Object[] { returnStatement, declaration});
                    }
                }

                super.visitJSBlock(element);
            }
        };
    }

    public void convertToClassPattern(PsiFile file)
    {
        file.acceptChildren(getDefineVisitor());
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
