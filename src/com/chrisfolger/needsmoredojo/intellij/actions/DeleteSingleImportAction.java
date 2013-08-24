package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.AMDImportLocator;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class DeleteSingleImportAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());

        AMDImportLocator.LocatedAMDImport amdImport = new AMDImportLocator().findNearestImport(element, psiFile);
    }
}
