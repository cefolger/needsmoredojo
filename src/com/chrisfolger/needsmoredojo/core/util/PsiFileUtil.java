package com.chrisfolger.needsmoredojo.core.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;

public class PsiFileUtil
{
    public static PsiFile getPsiFileInCurrentEditor(Project project)
    {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        if(editor == null)
        {
            return null;
        }

        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, editor.getProject());

        return psiFile;
    }
}
