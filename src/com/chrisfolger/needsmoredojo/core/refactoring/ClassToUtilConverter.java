package com.chrisfolger.needsmoredojo.core.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.CompletionCallback;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.DeclareResolver;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.DeclareStatementItems;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassToUtilConverter implements CompletionCallback
{
    @Override
    public void run(Object[] result)
    {
        DeclareResolver util = new DeclareResolver();

        final DeclareStatementItems utilItem = util.getDeclareStatementFromParsedStatement(result);
        CommandProcessor.getInstance().executeCommand(utilItem.getDeclareContainingStatement().getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        doRefactor(utilItem.getClassName(), utilItem.getDeclareContainingStatement(), utilItem.getExpressionsToMixin(), utilItem.getMethodsToConvert());
                    }
                });
            }
        },
        "Convert class module to util module",
        "Convert class module to util module");
    }

    public void doRefactor(@Nullable JSLiteralExpression className, @NotNull JSElement originalReturnStatement, @NotNull JSExpression[] mixins, @NotNull JSProperty[] properties) {
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

        String classPrefix = "";
        if(className != null)
        {
            classPrefix = String.format("%s, ", className.getText());
        }

        // create the declare statement and add it before the return statement
        String declareStatement = String.format("var util = declare(%s[%s], {});", classPrefix, mixinArray.toString());
        JSUtil.addStatementBeforeElement(parent, originalReturnStatement, declareStatement);

        // convert each property to an assignment statement
        for (JSProperty property : properties) {
            String propertyStatement = String.format("util.%s = %s;", property.getName(), property.getValue().getText().replaceAll("this", "util"));

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

    /**
     * This method returns a visitor that will take a dojo module and convert it to use a util pattern
     * instead of a class pattern
     *
     * @return the visitor
     */
    public JSRecursiveElementVisitor getVisitorToConvertToUtilPattern()
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
                function.acceptChildren(new DeclareResolver().getVisitorToRetrieveDeclare(new ClassToUtilConverter()));

                return;
            }
        };
    }

    public void convertToUtilPattern(PsiFile file)
    {
        // steps:
        // get the return declare statement
        // get all of the literal expressions

        file.acceptChildren(getVisitorToConvertToUtilPattern());
    }
}
