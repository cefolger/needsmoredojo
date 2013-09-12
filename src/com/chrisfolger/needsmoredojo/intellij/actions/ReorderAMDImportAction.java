package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.importing.ImportReorderer;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class ReorderAMDImportAction extends JavaScriptAction
{
    private final AMDPsiUtil.Direction direction;

    public ReorderAMDImportAction()
    {
        direction = AMDPsiUtil.Direction.UP;
    }

    public ReorderAMDImportAction(AMDPsiUtil.Direction direction)
    {
        this.direction = direction;
    }

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);

        if(editor == null || file == null)
        {
            return;
        }

        final PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        final ImportReorderer reorderer = new ImportReorderer();

        CommandProcessor.getInstance().executeCommand(file.getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        reorderer.doSwap(element, editor, direction);
                    }
                });
            }
        },
        "Reorder AMD Import",
        "Reorder AMD Import");
    }
}
