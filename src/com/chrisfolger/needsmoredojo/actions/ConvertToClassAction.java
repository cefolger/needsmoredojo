package com.chrisfolger.needsmoredojo.actions;

import com.chrisfolger.needsmoredojo.base.PsiFileUtil;
import com.chrisfolger.needsmoredojo.refactoring.DeclareFinder;
import com.chrisfolger.needsmoredojo.refactoring.UtilFinder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiFile;

public class ConvertToClassAction extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());

        new UtilFinder().convertToClassPattern(psiFile);
    }
}
