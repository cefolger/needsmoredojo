package com.chrisfolger.needsmoredojo.refactoring;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiElement;

public class UtilConverter implements DeclareFinder.CompletionCallback
{
    @Override
    public void run(Object[] result)
    {
        JSCallExpression expression = (JSCallExpression) result[0];
        final JSReturnStatement returnStatement = (JSReturnStatement) result[1];

        // this will be used to determine what we mixin to the util
        JSArrayLiteralExpression arrayLiteral = (JSArrayLiteralExpression) expression.getArguments()[0];
        final JSExpression[] expressionsToMixin = arrayLiteral.getExpressions();

        // now we need to get the object literal with all of the function names
        JSObjectLiteralExpression literal = (JSObjectLiteralExpression) expression.getArguments()[1];
        final JSProperty[] methodsToConvert = literal.getProperties();

        CommandProcessor.getInstance().executeCommand(expression.getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        doRefactor(returnStatement, expressionsToMixin, methodsToConvert);
                    }
                });
            }
        },
        "Convert module to use Util Pattern",
        "Convert module to use Util Pattern");
    }

    public void doRefactor(JSReturnStatement originalReturnStatement, JSExpression[] mixins, JSProperty[] properties) {
        // insert new items before this
        PsiElement parent = originalReturnStatement.getParent();

        StringBuilder mixinArray = new StringBuilder();
        for (JSExpression mixin : mixins) {
            if (mixinArray.toString().equals("")) {
                mixinArray.append(mixin.getText());
            } else {
                mixinArray.append(", " + mixin.getText());
            }
        }
        String declareStatement = String.format("var util = declare([%s], {});", mixinArray.toString());
        ASTNode node = JSChangeUtil.createStatementFromText(parent.getProject(), declareStatement, JSUtils.getDialect(parent.getContainingFile()));
        parent.addBefore(node.getPsi(), originalReturnStatement);

        for (JSProperty property : properties) {
            String propertyStatement = String.format("util.%s = %s;", property.getName(), property.getValue().getText());
            node = JSChangeUtil.createStatementFromText(parent.getProject(), propertyStatement);
            parent.addBefore(node.getPsi(), originalReturnStatement);
        }

        String newReturnStatement = "return util;";
        node = JSChangeUtil.createStatementFromText(parent.getProject(), newReturnStatement, JSUtils.getDialect(parent.getContainingFile()));
        parent.addBefore(node.getPsi(), originalReturnStatement);

        originalReturnStatement.delete();
    }
}
