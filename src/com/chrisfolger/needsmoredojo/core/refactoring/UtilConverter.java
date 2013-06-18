package com.chrisfolger.needsmoredojo.core.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.DeclareFinder;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiElement;

public class UtilConverter implements DeclareFinder.CompletionCallback
{
    public class UtilItem
    {
        private JSExpression[] expressionsToMixin;
        private JSProperty[] methodsToConvert;
        private JSReturnStatement returnStatement;

        public UtilItem(JSExpression[] expressionsToMixin, JSProperty[] methodsToConvert, JSReturnStatement returnStatement) {
            this.expressionsToMixin = expressionsToMixin;
            this.methodsToConvert = methodsToConvert;
            this.returnStatement = returnStatement;
        }

        public JSReturnStatement getReturnStatement() {
            return returnStatement;
        }

        public JSExpression[] getExpressionsToMixin() {
            return expressionsToMixin;
        }

        public JSProperty[] getMethodsToConvert() {
            return methodsToConvert;
        }
    }

    public UtilItem getDeclareStatementFromParsedStatement(Object[] result)
    {
        JSCallExpression expression = (JSCallExpression) result[0];
        JSReturnStatement returnStatement = (JSReturnStatement) result[1];

        // this will be used to determine what we mixin to the util
        JSExpression[] expressionsToMixin = new JSExpression[0];

        /*
            three possible syntax'es for declare:

            declare(null, {});
            declare([], {});
            declare(string, [], {});
         */
        int objectLiteralIndex = 1;
        if(expression.getArguments()[0] instanceof  JSArrayLiteralExpression)
        {
            JSArrayLiteralExpression arrayLiteral = (JSArrayLiteralExpression) expression.getArguments()[0];
            expressionsToMixin = arrayLiteral.getExpressions();
        }
        else if (expression.getArguments().length == 3 && expression.getArguments()[1] instanceof JSArrayLiteralExpression)
        {
            JSArrayLiteralExpression arrayLiteral = (JSArrayLiteralExpression) expression.getArguments()[1];
            expressionsToMixin = arrayLiteral.getExpressions();
            objectLiteralIndex = 2;
        }

        // now we need to get the object literal with all of the function names
        JSObjectLiteralExpression literal = (JSObjectLiteralExpression) expression.getArguments()[objectLiteralIndex];
        JSProperty[] methodsToConvert = literal.getProperties();

        return new UtilItem(expressionsToMixin, methodsToConvert, returnStatement);
    }

    @Override
    public void run(Object[] result)
    {
        final UtilItem utilItem = getDeclareStatementFromParsedStatement(result);
        CommandProcessor.getInstance().executeCommand(utilItem.getReturnStatement().getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        doRefactor(utilItem.getReturnStatement(), utilItem.getExpressionsToMixin(), utilItem.getMethodsToConvert());
                    }
                });
            }
        },
        "Convert class module to util module",
        "Convert class module to util module");
    }

    public void doRefactor(JSReturnStatement originalReturnStatement, JSExpression[] mixins, JSProperty[] properties) {
        // insert new items before the return statement
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
        String declareStatement = String.format("var util = declare([%s], {});", mixinArray.toString());
        JSUtil.addStatementBeforeElement(parent, originalReturnStatement, declareStatement);

        // convert each property to an assignment statement
        for (JSProperty property : properties) {
            String propertyStatement = String.format("util.%s = %s;", property.getName(), property.getValue().getText());
            JSUtil.addStatementBeforeElement(parent, originalReturnStatement, propertyStatement);
        }

        // add the final statement to return the util
        String newReturnStatement = "return util;";
        JSUtil.addStatementBeforeElement(parent, originalReturnStatement, newReturnStatement);

        // delete the old return declare(...) block
        originalReturnStatement.delete();
    }
}
