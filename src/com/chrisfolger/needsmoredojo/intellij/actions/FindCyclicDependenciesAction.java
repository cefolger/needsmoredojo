package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.intellij.inspections.CyclicDependencyInspection;
import com.chrisfolger.needsmoredojo.intellij.toolwindows.FindCyclicDependenciesToolWindow;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;

import java.util.List;
import java.util.Map;

public class FindCyclicDependenciesAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        ToolWindow window = ToolWindowManager.getInstance(e.getProject()).getToolWindow("FindCyclicDependencies");
        window.getComponent().removeAll();

        CyclicDependencyInspection inspection = new CyclicDependencyInspection();

        // TODO only get project sources
        for(VirtualFile file : FilenameIndex.getAllFilesByExt(e.getProject(), "js"))
        {
            if(DojoModuleFileResolver.isInDojoSources(file.getParent().getPath()))
            {
                continue;
            }

            PsiFile psiFile = PsiManager.getInstance(e.getProject()).findFile(file);

            try
            {
                inspection.checkFile(psiFile, InspectionManager.getInstance(e.getProject()), false);
            }
            catch(Exception ex)
            {
                // TODO write
            }
        }


        Map<String,List<String>> incriminatingModules = inspection.getIncriminatingModules();

        new FindCyclicDependenciesToolWindow().createContent(e.getProject(), window, incriminatingModules);
    }
}
