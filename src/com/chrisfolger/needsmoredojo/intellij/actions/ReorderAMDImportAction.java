package com.chrisfolger.needsmoredojo.intellij.actions;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class ReorderAMDImportAction extends JavaScriptAction
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

        // TODO move to testable section
        // test cases:
        // comma resolves to real element
        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        if(element.getText().equals(","))
        {
            element = element.getPrevSibling();
        }

        JSLiteralExpression source = (JSLiteralExpression) element;
        // find destination
        PsiElement node = source.getPrevSibling();
        int tries = 0;
        while(tries < 5)
        {
            if(node instanceof  JSLiteralExpression)
            {
                break;
            }

            node = node.getPrevSibling();
            tries ++;
        }

        if(!(node instanceof JSLiteralExpression))
        {
            return;
        }

        JSLiteralExpression destination = (JSLiteralExpression) node;
        destination.replace(source);
        source.replace(destination);

        int i=0;
    }
}
