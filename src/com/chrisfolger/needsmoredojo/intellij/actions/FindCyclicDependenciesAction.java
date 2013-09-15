package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.CyclicDependencyDetector;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.DependencyNode;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.DetectionResult;
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

        CyclicDependencyDetector detector = new CyclicDependencyDetector();

        // TODO only get project sources
        for(VirtualFile file : FilenameIndex.getAllFilesByExt(e.getProject(), "js"))
        {
            if(DojoModuleFileResolver.isInDojoSources(file.getPath()))
            {
                continue;
            }

            PsiFile psiFile = PsiManager.getInstance(e.getProject()).findFile(file);

            try
            {
                DependencyNode cycle = detector.addDependenciesOfFile(psiFile, psiFile.getProject(), psiFile, null, null);

                if(cycle != null)
                {
                    DetectionResult cycleDetectionResult = detector.getCycleDetectionResult(cycle);
                    detector.updateIncriminatingModules(cycleDetectionResult.getDependencies(), cycleDetectionResult.getCyclePath());
                }
            }
            catch(Exception ex)
            {
                // TODO write
                int i=0;
            }
        }


        Map<String,List<String>> incriminatingModules = detector.getIncriminatingModules();
        new FindCyclicDependenciesToolWindow().createContent(e.getProject(), window, incriminatingModules);
    }
}
