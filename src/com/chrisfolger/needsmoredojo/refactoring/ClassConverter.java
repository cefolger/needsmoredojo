package com.chrisfolger.needsmoredojo.refactoring;

import com.chrisfolger.needsmoredojo.base.JSUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiElement;

import java.util.List;

public class ClassConverter implements DeclareFinder.CompletionCallback
{
    @Override
    public void run(Object[] result)
    {
        final JSReturnStatement returnStatement = (JSReturnStatement) result[0];
        JSCallExpression declaration = (JSCallExpression) result[1];
        final List<JSExpressionStatement> methods = (List<JSExpressionStatement>) result[2];

        final JSExpression[] mixins = ((JSArrayLiteralExpression) declaration.getArguments()[0]).getExpressions();

        CommandProcessor.getInstance().executeCommand(returnStatement.getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        doRefactor(mixins, methods, returnStatement);
                    }
                });
            }
        },
        "Convert util module to class module",
        "Convert util module to class module");
    }

    public void doRefactor(JSExpression[] mixins, List<JSExpressionStatement> methods, JSReturnStatement originalReturnStatement)
    {
        PsiElement parent = originalReturnStatement.getParent();

        // build an array of mixins for the new declare statement
        StringBuilder mixinArray = new StringBuilder();
        for (JSExpression mixin : mixins) {
            if (mixinArray.toString().equals("")) {
                mixinArray.append(mixin.getText());
            } else {
                mixinArray.append(", " + mixin.getText());
            }
        }

        // create the declare statement and add it before the return statement
        String declareStatement = String.format("return declare([%s], {\n\n});", mixinArray.toString());
        PsiElement declareExpression = JSUtil.createStatement(parent, declareStatement);

        JSObjectLiteralExpression literal = (JSObjectLiteralExpression) ((JSCallExpression) declareExpression
                .getChildren()[0])
                .getArguments()[1];

        for(JSExpressionStatement method : methods)
        {
            JSAssignmentExpression expression = (JSAssignmentExpression) method.getExpression();
            String definition = ((JSReferenceExpression)((JSDefinitionExpression) expression
                    .getChildren()[0])
                    .getExpression())
                    .getReferencedName();

            String content = expression.getChildren()[1].getText();
            literal.getNode().addChild(JSUtil.createStatement(literal, String.format("%s: %s", definition, content)).getNode());
        }

        parent.addBefore(declareExpression, originalReturnStatement);
    }
}
