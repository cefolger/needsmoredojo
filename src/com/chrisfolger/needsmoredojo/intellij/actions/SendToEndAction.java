package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.AMDImport;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportReorderer;
import com.chrisfolger.needsmoredojo.core.amd.importing.InvalidDefineException;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSParameterList;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;

public class SendToEndAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);

        final PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        if(element == null)
        {
            return;
        }

        ImportReorderer reorderer = new ImportReorderer();
        PsiElement[] results = reorderer.getSourceAndDestination(element, AMDPsiUtil.Direction.NONE);

        if(results.length == 0)
        {
            new Notification("needsmoredojo", "Send AMD Import to End", "No valid import found", NotificationType.WARNING).notify(file.getProject());
            return;
        }

        final PsiElement define = results[0];
        PsiElement parameter = null;

        // FIXME a few special cases (as always) that should result in an early exit
        //  when there are no imports
        //  when there is only one import

        List<PsiElement> defines = new ArrayList<PsiElement>();
        List<PsiElement> parameters = new ArrayList<PsiElement>();

        DefineResolver resolver = new DefineResolver();
        final DefineStatement importBlock = resolver.getNearestImportBlock(define);

        if(importBlock == null)
        {
            new Notification("needsmoredojo", "Send AMD Import to End", "No valid import found", NotificationType.WARNING).notify(file.getProject());
            return;
        }

        try {
            resolver.addDefinesAndParametersOfImportBlock(importBlock.getCallExpression(), defines, parameters);
        } catch (InvalidDefineException e1) { /* not interested in this failure case */ }

        for (int i = 0; i < defines.size(); i++)
        {
            if(defines.get(i).equals(define))
            {
                parameter = parameters.get(i);
                break;
                // FIXME if parameter is out of index
            }
        }

        // go to the last literal/parameter pair
        PsiElement lastLiteral = null;
        PsiElement lastParameter = null;

        if(defines.size() > parameters.size())
        {
            lastLiteral = defines.get(parameters.size()-1);
            lastParameter = parameters.get(parameters.size()-1);
        }
        else
        {
            lastLiteral = defines.get(defines.size()-1);
            lastParameter = parameters.get(defines.size()-1);
        }

        final PsiElement finalParameter = parameter;
        final PsiElement finalLastLiteral = lastLiteral;
        final PsiElement finalLastParameter = lastParameter;
        CommandProcessor.getInstance().executeCommand(file.getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        moveElementToEnd(define, finalParameter, finalLastLiteral, finalLastParameter, importBlock);
                    }
                });
            }
        },
        "Send AMD Import to End",
        "Send AMD Import to End");
    }

    private PsiElement moveImportToEnd(JSArrayLiteralExpression imports, JSParameterList parameters, String module, String parameter, PsiElement lastDefine, PsiElement lastParameter)
    {
        // TODO move to AMDPsiUtil if we need to reuse this in the future
        PsiElement lastChild = imports.getChildren()[imports.getChildren().length-1];

        if(lastDefine != null)
        {
            lastChild = lastDefine;
        }

        PsiElement element = imports.addAfter(JSChangeUtil.createExpressionFromText(imports.getProject(), String.format("%s", module)).getPsi(), lastChild);
        imports.getNode().addLeaf(JSTokenTypes.COMMA, ",", element.getNode());
        imports.getNode().addLeaf(JSTokenTypes.WHITE_SPACE, "\n", element.getNode());

        PsiElement lastParameterChild = parameters.getChildren()[parameters.getChildren().length-1];

        if(lastParameter != null)
        {
            lastParameterChild = lastParameter;
        }

        PsiElement parameterElement = parameters.addAfter(JSChangeUtil.createExpressionFromText(imports.getProject(), String.format("%s", parameter)).getPsi(), lastParameterChild);
        parameters.getNode().addLeaf(JSTokenTypes.COMMA, ",", parameterElement.getNode());

        return element;
    }

    private void moveElementToEnd(PsiElement define, PsiElement parameter, PsiElement lastDefine, PsiElement lastParameter, DefineStatement defineStatement)
    {
        if(lastDefine.equals(define))
        {
            return;
        }

        PsiElement ignoreComment = AMDPsiUtil.getIgnoreCommentAfterLiteral(define);
        PsiElement newElement = moveImportToEnd(defineStatement.getArguments(), defineStatement.getFunction().getParameterList(), define.getText(), parameter.getText(), lastDefine, lastParameter);

        if(ignoreComment != null)
        {
            defineStatement.getArguments().addAfter(ignoreComment, newElement);
        }

        AMDPsiUtil.removeSingleImport(new AMDImport((JSElement) define, (JSElement)parameter));
    }
}
