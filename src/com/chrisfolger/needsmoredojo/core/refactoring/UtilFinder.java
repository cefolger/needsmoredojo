package com.chrisfolger.needsmoredojo.core.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.CompletionCallback;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;

public class UtilFinder
{
    public JSElementVisitor getReturnCallbackVisitor(final CompletionCallback onReturnFound)
    {
        return new JSElementVisitor() {
            @Override
            public void visitJSBlock(JSBlockStatement element)
            {
                JSReturnStatement returnStatement = null;
                JSCallExpression declaration = null;
                JSVarStatement declarationVariable = null;
                List<JSExpressionStatement> otherElements = new ArrayList<JSExpressionStatement>();

                for(JSStatement statement : element.getStatements())
                {
                    if(statement instanceof JSReturnStatement)
                    {
                        returnStatement = (JSReturnStatement) statement;
                    }
                    else if (statement instanceof JSVarStatement && statement.getText().contains("declare("))
                    {
                        declarationVariable = (JSVarStatement) statement;

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
                    else if (statement instanceof JSExpressionStatement)
                    {
                        otherElements.add((JSExpressionStatement) statement);
                    }
                }

                if(returnStatement != null && declaration != null)
                {
                    onReturnFound.run(new Object[] { declaration,returnStatement, otherElements, declarationVariable});
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
                function.acceptChildren(getReturnCallbackVisitor(new UtilToClassConverter()));

                return;
            }
        };
    }
}
