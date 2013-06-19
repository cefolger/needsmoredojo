package com.chrisfolger.needsmoredojo.core.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.DeclareFinder;
import com.chrisfolger.needsmoredojo.core.util.DeclareUtil;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiElement;

public class UtilConverter implements DeclareFinder.CompletionCallback
{
    @Override
    public void run(Object[] result)
    {
        DeclareUtil util = new DeclareUtil();

        final DeclareUtil.DeclareStatementItems utilItem = util.getDeclareStatementFromParsedStatement(result);
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

            // account for the case where the class property is in quotes, because it might be a special
            // property that would not be a valid javascript property unless it was in quotes.
            if(property.getNameIdentifier().getText().contains("'") || property.getNameIdentifier().getText().contains("\""))
            {
                propertyStatement = String.format("util[%s] = %s;", property.getNameIdentifier().getText(), property.getValue().getText());
            }

            JSUtil.addStatementBeforeElement(parent, originalReturnStatement, propertyStatement);
        }

        // add the final statement to return the util
        String newReturnStatement = "return util;";
        JSUtil.addStatementBeforeElement(parent, originalReturnStatement, newReturnStatement);

        // delete the old return declare(...) block
        originalReturnStatement.delete();
    }
}
