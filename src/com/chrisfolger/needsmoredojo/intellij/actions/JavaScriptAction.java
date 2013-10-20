package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;

public abstract class JavaScriptAction extends AnAction
{
    protected boolean fileAgnostic()
    {
        return false;
    }

    protected boolean supportsFileType(PsiFile file)
    {
        if(file == null || file.getProject() == null)
        {
            return false;
        }

        DojoSettings settings = ServiceManager.getService(file.getProject(), DojoSettings.class);
        String[] fileTypes = settings.getSupportedFileTypes().split(",");
        for(String type : fileTypes)
        {
            if(type.trim().equals(file.getVirtualFile().getExtension()))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void update(AnActionEvent e)
    {
        if(e.getProject() == null)
        {
            e.getPresentation().setEnabled(false);
            return;
        }

        if(!ServiceManager.getService(e.getProject(), DojoSettings.class).isNeedsMoreDojoEnabled())
        {
            e.getPresentation().setEnabled(false);
            return;
        }
        else
        {
            e.getPresentation().setEnabled(true);
        }

        if(fileAgnostic())
        {
            e.getPresentation().setEnabled(true);
            return;
        }

        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());
        if(psiFile == null)
        {
            e.getPresentation().setEnabled(false);
            return;
        }

        if(!supportsFileType(psiFile))
        {
            e.getPresentation().setEnabled(false);
            return;
        }

        e.getPresentation().setEnabled(true);
    }
}
