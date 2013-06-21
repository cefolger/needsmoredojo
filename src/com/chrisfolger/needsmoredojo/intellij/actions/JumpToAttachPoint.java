package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.DeclareFinder;
import com.chrisfolger.needsmoredojo.core.util.TemplatedWidgetUtil;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class JumpToAttachPoint extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);

        if(editor == null || file == null)
        {
            return;
        }

        // TODO base on source roots
        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        new TemplatedWidgetUtil(file,  new DeclareFinder.CompletionCallback() {
            @Override
            public void run(Object[] result) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        }).findTemplatePath();

        int i=0;
    }

    @Override
    public void update(AnActionEvent e)
    {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);

        e.getPresentation().setEnabled(true);

        if(editor == null || file == null)
        {
            e.getPresentation().setEnabled(false);
        }
    }
}
