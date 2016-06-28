package com.chrisfolger.needsmoredojo.core.amd.importing;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameException;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.refactoring.MatchResult;
import com.chrisfolger.needsmoredojo.core.util.JSUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a class for any actions that affect an existing import
 */
public class ImportUpdater
{
    private List<NameException> moduleNamingExceptionMap;

    public ImportUpdater(@Nullable List<NameException> moduleNamingExceptionMap)
    {
        if(moduleNamingExceptionMap == null)
        {
            this.moduleNamingExceptionMap = new ArrayList<NameException>();
        }
        else
        {
            this.moduleNamingExceptionMap = moduleNamingExceptionMap;
        }
    }

    /**
     * Updates an existing AMD module reference (define literal + the parameter)
     *
     * @param targetFile the file containing the reference
     * @param match a MatchResult that represents the new module reference
     * @param statement the define or require statement that is being updated
     * @param replacementExpression an expression that will replace the define literal expression
     * @param updateReferences if true, will update references to the parameter if it is renamed
     */
    public void updateModuleReference(final PsiFile targetFile, final MatchResult match, final DefineStatement statement, final PsiElement replacementExpression, final boolean updateReferences)
    {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                PsiElement defineLiteral = statement.getArguments().getExpressions()[match.getIndex()];
                defineLiteral.replace(replacementExpression);

                if(!updateReferences)
                {
                    return;
                }

                // sometimes the lengths of the imports don't match up due to plugins etc.
                if(!(match.getIndex() >= statement.getFunction().getParameters().length))
                {
                    // for performance reasons we should only rename a parameter if the name has actually changed
                    String parameterText = statement.getFunction().getParameterVariables()[match.getIndex()].getText();
                    String newParameterName = NameResolver.defineToParameter(match.getPath(), moduleNamingExceptionMap);

                    if(parameterText.equals(newParameterName))
                    {
                        return;
                    }

                    RenameRefactoring refactoring = RefactoringFactory.getInstance(targetFile.getProject())
                            .createRename(statement.getFunction().getParameterVariables()[match.getIndex()], newParameterName, false, false);

                    refactoring.doRefactoring(refactoring.findUsages());
                }
            }
        });
    }

    /**
     * Updates a module's import reference with a new location
     *
     * @param targetFile the module to update
     * @param match the match that holds the location of the import to update
     * @param statement the module's parsed define statement
     */
    public void updateModuleReference(final PsiFile targetFile, final MatchResult match, final DefineStatement statement, boolean updateReferences)
    {
        PsiElement defineLiteral = statement.getArguments().getExpressions()[match.getIndex()];
        updateModuleReference(targetFile, match, statement, JSUtil.createExpression(defineLiteral.getParent(), match.getQuote() + match.getPath() + match.getPluginResourceId() + match.getQuote()), updateReferences);
    }
}
