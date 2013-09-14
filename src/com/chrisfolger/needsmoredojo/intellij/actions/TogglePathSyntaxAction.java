package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourceLibrary;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportReorderer;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportResolver;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.LinkedHashMap;

public class TogglePathSyntaxAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        // look up the current import
        // get the choices
        // switch to the other one

        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);

        final PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());

        final PsiElement define = new ImportReorderer().getSourceAndDestination(element, AMDPsiUtil.Direction.NONE)[0];
        boolean relative = define.getText().charAt(1) == '.';
        String moduleName = NameResolver.getModuleName(define.getText().replaceAll("'", "").replaceAll("\"", ""));

        // get the list of possible strings/PsiFiles that would match it
        PsiFile[] files = new ImportResolver().getPossibleDojoImportFiles(file.getProject(), moduleName, true);

        // get the files that are being imported
        // TODO performance optimization
        String[] results = new ImportResolver().getChoicesFromFiles(files, new SourcesLocator().getSourceLibraries(file.getProject()).toArray(new SourceLibrary[0]), moduleName, define.getContainingFile(), false);
        String choice = results[0];

        if(results.length > 1)
        {
            if(results[1].startsWith(".") && !relative)
            {
                choice = results[1];
            }
            else if (!results[1].startsWith(".") && relative)
            {
                choice = results[1];
            }
            // TODO refactor
            // TODO bug if you have cursor over a plugin resource string.
        }

        final PsiElement replacement = JSChangeUtil.createExpressionFromText(define.getProject(), "\"" + choice + "\"").getPsi();

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
}
