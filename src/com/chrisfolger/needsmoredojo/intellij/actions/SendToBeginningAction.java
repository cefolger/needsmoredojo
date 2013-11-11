package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.AMDImport;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportCreator;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;

import java.util.List;

public class SendToBeginningAction extends SendToAction
{
    @Override
    protected String getName()
    {
        return "Send AMD Import to Beginning";
    }

    @Override
    protected void moveAction(AnActionEvent e, PsiElement define, PsiElement parameter, List<PsiElement> defines, List<PsiElement> parameters, DefineStatement defineStatement)
    {
        String defineText = define.getText().replaceAll("\"|'", "");
        String parameterText = parameter.getText();

        AMDPsiUtil.removeSingleImport(new AMDImport((JSElement) define, (JSElement) parameter));

        new ImportCreator().createImport(defineText, parameterText, defineStatement.getArguments(), defineStatement.getFunction().getParameterList());

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        int index = defineStatement.getArguments().getFirstChild().getTextOffset();
        editor.getScrollingModel().scrollVertically(index);
    }
}
