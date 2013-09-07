package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.refactoring.ClassToUtilConverter;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiFile;

public class ConvertToUtilAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());

        new ClassToUtilConverter().convertToUtilPattern(psiFile);
    }
}
