package com.chrisfolger.needsmoredojo.core.amd.importing;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameException;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ImportCreator
{
    private void displayDuplicateMessage(String parameter, Project project)
    {
        new Notification("needsmoredojo", "Add new AMD import", parameter + " is already defined ", NotificationType.INFORMATION).notify(project);
    }

    public void createImport(String module, String quoteCharacter, String parameter, JSArrayLiteralExpression imports, JSParameterList parameters)
    {
        for(JSParameter element : parameters.getParameterVariables())
        {
            if(element.getName().equals(parameter))
            {
                // already defined, so just exit
                displayDuplicateMessage(parameter, parameters.getProject());
                return;
            }
        }

        if(imports.getExpressions().length == 0)
        {
            // how to insert
            /*
                a few cases to consider:
                define([

                ])

                In my opinion, this is the most readable and the one ImportCreator will account for best:
                define([
                ])

                define([])
             */
            String defineText = imports.getText();
            String formatString = quoteCharacter + "%s" + quoteCharacter;
            String moduleID = String.format(formatString, module);
            if(defineText.contains("\n\n"))
            {
                JSUtil.addStatementBeforeElement(imports, imports.getLastChild(), moduleID, "\n");
            }
            else if(defineText.contains("\n"))
            {
                JSUtil.addStatementBeforeElement(imports, imports.getLastChild(), moduleID, "\n");
            }
            else
            {
                JSUtil.addStatementBeforeElement(imports, imports.getLastChild(), moduleID, "");
            }

            if(parameters.getChildren().length == 0)
            {
                JSUtil.addStatementBeforeElement(parameters, parameters.getLastChild(), parameter, "");
            }
            else
            {
                JSUtil.addStatementBeforeElement(parameters, parameters.getChildren()[0], parameter + ",", " ");
            }
        }
        else
        {
            String formatString = quoteCharacter + "%s" + quoteCharacter + ",";
            JSUtil.addStatementBeforeElement(imports, imports.getExpressions()[0], String.format(formatString, module), "\n");
            if(parameters.getChildren().length > 0)
            {
                JSUtil.addStatementBeforeElement(parameters, parameters.getChildren()[0], parameter + ",", " ");
            }
            else
            {
                parameters.addAfter(JSUtil.createStatement(parameters, parameter), parameters.getFirstChild());
            }
        }
    }

    public void createImport(String module, String quoteCharacter, JSArrayLiteralExpression imports, JSParameterList parameters)
    {
        List<NameException> exceptionsMap = ServiceManager.getService(parameters.getProject(), DojoSettings.class).getNamingExceptionList();
        String parameter = NameResolver.defineToParameter(module, exceptionsMap);

        // if the parameter would cause a duplicate, then assume it is a module with a different path but the same name
        // as an existing imported module.
        for (int i = 0; i < parameters.getParameterVariables().length; i++)
        {
            JSParameter existingParameter = parameters.getParameterVariables()[i];
            if(existingParameter != null && existingParameter.getText().equals(parameter))
            {
                String existingAbsolutePath = NameResolver.getModuleAndPathWithoutPluginResourceId(imports.getExpressions()[i].getText().replaceAll("'", "").replaceAll("\"", ""));
                if(existingAbsolutePath.contains("."))
                {
                    existingAbsolutePath = new ImportReorderer().getPathSyntax(parameters.getProject(), existingAbsolutePath, parameters.getContainingFile(), false);
                }

                String absolutePath = new ImportReorderer().getPathSyntax(parameters.getProject(), module, parameters.getContainingFile(), false);

                if(existingAbsolutePath.equals(absolutePath))
                {
                    // unless of course the two modules are actually identical
                    displayDuplicateMessage(parameter, parameters.getProject());
                    return;
                }
                String parameterWithAbsolutePath = NameResolver.defineToParameter(module,
                        exceptionsMap,
                        true,
                        absolutePath);

                createImport(module, quoteCharacter, parameterWithAbsolutePath, imports, parameters);
                return;
            }
        }

        createImport(module, quoteCharacter, parameter, imports, parameters);
    }

    /**
     * entry point for adding an AMD import to an existing define statement
     *
     * @param file the file that the import will be added to
     * @param module the name of the module the user wants to add
     * @return true if the module was added, false otherwise
     */
    public boolean addImport(final PsiFile file, final String module)
    {
        DefineStatement items = new DefineResolver().getDefineStatementItems(file);

        if(items == null)
        {
            return false;
        }
        return addImport(file, module, items);
    }

    public boolean addImport(final PsiFile file, final String module, DefineStatement statementToAddTo)
    {
        DojoSettings settings = ServiceManager.getService( file.getProject(), DojoSettings.class);
        String quoteCharacter = settings.isSingleQuotedModuleIDs() ? "'" : "\"";
        createImport(module, quoteCharacter, statementToAddTo.getArguments(), statementToAddTo.getFunction().getParameterList());
        return true;
    }


    /**
     * when the user adds a new import, this code searches for the nearest possible element
     * to the cursor that they may have wanted to import and returns a suggested choice.
     *
     * I know this method is crude/hard to read and could be way more elegant, however it's good enough for now
     * and produces quite a lot of benefit for low effort
     *
     * TODO this is a good candidate for unit testing...
     */
    public String getSuggestedImport(@Nullable PsiElement element)
    {
        if(element == null)
        {
            return "";
        }

        String initialChoice = "";
        PsiElement parent = element.getParent();
        PsiElement previousSibling = element.getPrevSibling();

        // (underscore represents cursor)
        // we're just over a reference. Example: Site_Util
        if (element.getParent() != null && element.getParent() instanceof JSReferenceExpression)
        {
            initialChoice = element.getText();
        }
        // we're inside a constructor. Example: new Button({_});
        if(element.getParent() instanceof JSObjectLiteralExpression)
        {
            JSObjectLiteralExpression literal = (JSObjectLiteralExpression) element.getParent();
            if(literal.getParent() != null && literal.getParent().getParent() != null && literal.getParent().getParent() instanceof JSNewExpression)
            {
                initialChoice = ((JSNewExpression)literal.getParent().getParent()).getMethodExpression().getText();
            }
        }
        // we're inside a new expression Example: new Button_
        if(parent != null && element.getParent().getParent() != null && parent.getParent() instanceof JSNewExpression)
        {
            initialChoice = ((JSNewExpression)parent.getParent()).getMethodExpression().getText();
        }
        // we're right after a new expression. Example: new Button({}) _
        else if (previousSibling != null && previousSibling.getChildren().length > 0 && previousSibling.getChildren()[0] instanceof JSNewExpression)
        {
            initialChoice = ((JSNewExpression)previousSibling.getChildren()[0]).getMethodExpression().getText();
        }
        // right after a reference. Example: SiteUtil_
        else if (previousSibling != null && previousSibling.getChildren().length > 0 && previousSibling.getChildren()[0] instanceof JSReferenceExpression)
        {
            initialChoice = previousSibling.getChildren()[0].getText();
        }
        // after a variable declaration. Example: var x = new Button({})_
        else if (previousSibling != null && element.getPrevSibling() instanceof JSVarStatement)
        {
            JSVarStatement statement = (JSVarStatement) element.getPrevSibling();
            for(JSVariable variable : statement.getVariables())
            {
                if(variable.getInitializer() instanceof JSNewExpression)
                {
                    JSNewExpression expression = (JSNewExpression) variable.getInitializer();
                    // if these conditions are false, it just means the new expression is not complete
                    if(expression != null && expression.getMethodExpression() != null)
                    {
                        initialChoice = expression.getMethodExpression().getText();
                    }
                }
            }
        }

        return initialChoice;
    }
}
