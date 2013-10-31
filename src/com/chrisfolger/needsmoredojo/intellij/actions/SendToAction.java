package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportReorderer;
import com.chrisfolger.needsmoredojo.core.amd.importing.InvalidDefineException;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
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

public abstract class SendToAction extends JavaScriptAction
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

        final List<PsiElement> defines = new ArrayList<PsiElement>();
        final List<PsiElement> parameters = new ArrayList<PsiElement>();

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

        final PsiElement finalParameter = parameter;
        CommandProcessor.getInstance().executeCommand(file.getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        moveAction(define, finalParameter, defines, parameters, importBlock);
                    }
                });
            }
        },
        getName(),
        getName());
    }

    protected abstract String getName();

    protected abstract void moveAction(PsiElement define, PsiElement parameter, List<PsiElement> defines, List<PsiElement> parameters, DefineStatement defineStatement);
}
