package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.CyclicDependencyDetector;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.DependencyNode;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.DetectionResult;
import com.chrisfolger.needsmoredojo.intellij.toolwindows.FindCyclicDependenciesToolWindow;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FindCyclicDependenciesAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        ToolWindow window = ToolWindowManager.getInstance(e.getProject()).getToolWindow("FindCyclicDependencies");
        window.getComponent().removeAll();
        window.setAvailable(false, null);
        window.hide(null);

        CyclicDependencyDetector detector = new CyclicDependencyDetector();

        Collection<VirtualFile> filesToSearch = new DojoModuleFileResolver().getAllDojoProjectSourceFiles(e.getProject());

        int count = 0;
        for(VirtualFile file : filesToSearch)
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
                    count++;
                }
            }
            catch(Exception ex)
            {
                // TODO write
                int i=0;
            }
        }


        if(count == 0)
        {
            new Notification("needsmoredojo", "Find Cyclic Dependencies", "No cycles were found in the dependency graph", NotificationType.INFORMATION).notify(e.getProject());
        }
        else
        {
            window.setAvailable(true, null);
            window.show(null);
            window.activate(null);

            Map<String,List<String>> incriminatingModules = detector.getIncriminatingModules();
            new FindCyclicDependenciesToolWindow().createContent(e.getProject(), window, incriminatingModules, count);
        }
    }
}
