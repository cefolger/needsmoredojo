package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.importing.ImportReorderer;
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

/**
 * This action allows you to toggle an AMD import between using a relative syntax or absolute syntax, if possible.
 */
public class TogglePathSyntaxAction extends JavaScriptAction
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
        final PsiElement define = reorderer.getSourceAndDestination(element, AMDPsiUtil.Direction.NONE)[0];

        final PsiElement replacement = reorderer.getOppositePathSyntaxFromImport(define, file);
        if(replacement != null)
        {
            CommandProcessor.getInstance().executeCommand(file.getProject(), new Runnable() {
                @Override
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            define.replace(replacement);
                        }
                    });
                }
            },
            "Toggle AMD Import Path Syntax",
            "Toggle AMD Import Path Syntax");
        }
        else
        {
            new Notification("needsmoredojo", "Toggle AMD Import Path Syntax", "No alternative syntax found", NotificationType.WARNING).notify(file.getProject());
        }
    }
}
