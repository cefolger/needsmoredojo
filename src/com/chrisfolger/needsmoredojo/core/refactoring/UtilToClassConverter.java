package com.chrisfolger.needsmoredojo.core.refactoring;

import com.chrisfolger.needsmoredojo.core.amd.CompletionCallback;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.DeclareResolver;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.DeclareStatementItems;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UtilToClassConverter implements CompletionCallback
{
    @Override
    public void run(Object[] result)
    {
        final DeclareStatementItems item = new DeclareResolver().getDeclareStatementFromParsedStatement(result);

        if(item == null)
        {
            Notifications.Bus.notify(new Notification("needsmoredojo", "Convert util module to class module", "Valid declare block was not found", NotificationType.WARNING));
            return;
        }

        final List<JSExpressionStatement> methods = (List<JSExpressionStatement>) result[2];
        final JSVarStatement declarationVariable = (JSVarStatement) result[3];

        CommandProcessor.getInstance().executeCommand(item.getDeclareContainingStatement().getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        PsiElement parent = item.getDeclareContainingStatement().getParent();
                        doRefactor(item.getClassName(), item.getExpressionsToMixin(), methods, item.getDeclareContainingStatement(), declarationVariable);
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

    public @NotNull String buildUtilPatternString(@Nullable JSLiteralExpression className, @NotNull JSExpression[] mixins, @NotNull List<JSExpressionStatement> methods, @NotNull String utilVariableName)
    {
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
            JSExpression reference = ((JSDefinitionExpression) expression.getChildren()[0]).getExpression();
            String definition = "";

            /* a util property can be two things:
                util.a = b;
                util['a'] = b;
             */
            if(reference instanceof JSReferenceExpression)
            {
                definition = ((JSReferenceExpression)reference).getReferencedName();
            }
            else if (reference instanceof JSIndexedPropertyAccessExpression)
            {
                definition = ((JSIndexedPropertyAccessExpression)reference).getIndexExpression().getText();
            }

            String content = expression.getChildren()[1].getText().replaceAll(utilVariableName, "this");

            if(i < methods.size() - 1)
            {
                properties.append(String.format("%s: %s,\n\n", definition, content));
            }
            else
            {
                properties.append(String.format("%s: %s", definition, content));
            }
        }

        String classPrefix = "";
        if(className != null)
        {
            classPrefix = String.format("%s, ", className.getText());
        }

        String declareStatement = String.format("return declare(%s[%s], {\n%s\n});", classPrefix, mixinArray.toString(), properties.toString());
        return declareStatement;
    }

    public void doRefactor(JSLiteralExpression className, JSExpression[] mixins, List<JSExpressionStatement> methods, JSElement originalReturnStatement, JSVarStatement declarationVariable)
    {
        PsiElement parent = originalReturnStatement.getParent();

        JSVariable variable = declarationVariable.getVariables()[0];

        String declareStatement = buildUtilPatternString(className, mixins, methods, variable.getName());
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

    public void convertToClassPattern(PsiFile file)
    {
        file.acceptChildren(new UtilFinder().getDefineVisitor());
    }
}
