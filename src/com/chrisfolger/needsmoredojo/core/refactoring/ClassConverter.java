package com.chrisfolger.needsmoredojo.core.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.DeclareFinder;
import com.chrisfolger.needsmoredojo.core.util.DeclareUtil;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
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
        final DeclareUtil.DeclareStatementItems item = new DeclareUtil().getDeclareStatementFromParsedStatement(result);

        final List<JSExpressionStatement> methods = (List<JSExpressionStatement>) result[2];
        final JSVarStatement declarationVariable = (JSVarStatement) result[3];

        CommandProcessor.getInstance().executeCommand(item.getReturnStatement().getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        PsiElement parent = item.getReturnStatement().getParent();
                        doRefactor(item.getExpressionsToMixin(), methods, item.getReturnStatement(), declarationVariable);
                        // all of those deletions creates a ton of whitespace for some reason. So, delete it!
                        cleanupWhiteSpace(parent);
                    }
                });
            }
        },
        "Convert util module to class module",
        "Convert util module to class module");
    }

    public void cleanupWhiteSpace(PsiElement parent)
    {
        String result = "{\n" + parent.getText().substring(1).trim();
        parent.replace(JSUtil.createStatement(parent, result));
    }

    public void doRefactor(JSExpression[] mixins, List<JSExpressionStatement> methods, JSReturnStatement originalReturnStatement, JSVarStatement declarationVariable)
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

        // convert each method to a property in the new object literal
        StringBuilder properties = new StringBuilder();
        for(int i=0;i<methods.size();i++)
        {
            JSExpressionStatement method = methods.get(i);

            JSAssignmentExpression expression = (JSAssignmentExpression) method.getExpression();
            String definition = ((JSReferenceExpression)((JSDefinitionExpression) expression
                    .getChildren()[0])
                    .getExpression())
                    .getReferencedName();

            String content = expression.getChildren()[1].getText();

            if(i < methods.size() - 1)
            {
                properties.append(String.format("%s: %s,\n\n", definition, content));
            }
            else
            {
                properties.append(String.format("%s: %s", definition, content));
            }
        }

        // create the new declare statement and add it before the return statement
        String declareStatement = String.format("return declare([%s], {\n%s\n});", mixinArray.toString(), properties.toString());
        PsiElement declareExpression = JSUtil.createStatement(parent, declareStatement);
        parent.addBefore(declareExpression, originalReturnStatement);

        // delete all of the old code
        for(JSExpressionStatement method : methods)
        {
            method.delete();
        }
        originalReturnStatement.delete();
        declarationVariable.delete();
    }
}
