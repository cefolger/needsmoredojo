package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.CyclicDependencyDetector;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.DependencyNode;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.DetectionResult;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.intellij.toolwindows.FindCyclicDependenciesToolWindow;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This action runs a task that scans the dependency graph for your sources. It will list cycles in the graph
 * in a separate tool window at the bottom of the IDE.
 */
public class FindCyclicDependenciesAction extends JavaScriptAction
{
    private Logger logger = Logger.getLogger(FindCyclicDependenciesAction.class);

    private void updateToolWindow(int count, final Project project, final CyclicDependencyDetector detector)
    {
        final int finalCount = count;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ToolWindowManager.getInstance(project).unregisterToolWindow("Find Cyclic AMD Dependencies");
                ToolWindow window = ToolWindowManager.getInstance(project).registerToolWindow("Find Cyclic AMD Dependencies", true, ToolWindowAnchor.BOTTOM);
                window.setTitle("Find Cyclic AMD Dependencies");
                window.setDefaultState(ToolWindowAnchor.BOTTOM, ToolWindowType.DOCKED, null);
                window.show(null);
                window.activate(null);

                Map<String, List<String>> incriminatingModules = detector.getIncriminatingModules();
                new FindCyclicDependenciesToolWindow().createContent(project, window, incriminatingModules, finalCount);
            }
        });
    }

    @Override
    protected boolean fileAgnostic()
    {
        return true;
    }

    @Override
    public void update(AnActionEvent e) {
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
    }

    @Override
    public void actionPerformed(final AnActionEvent e)
    {
        if(e.getProject() == null)
        {
            return;
        }

        final ProgressManager instance = ProgressManager.getInstance();

        instance.runProcessWithProgressSynchronously(new Runnable() {
            @Override
            public void run() {
                try
                {
                    instance.getProgressIndicator().setIndeterminate(true);

                    final CyclicDependencyDetector detector = new CyclicDependencyDetector();
                    Collection<VirtualFile> filesToSearch = new DojoModuleFileResolver().getAllDojoProjectSourceFiles(e.getProject());

                    int count = 0;
                    for (VirtualFile file : filesToSearch)
                    {
                        if (DojoModuleFileResolver.isInDojoSources(file.getPath()))
                        {
                            continue;
                        }

                        try
                        {
                            PsiFile psiFile = PsiManager.getInstance(e.getProject()).findFile(file);
                            DependencyNode cycle = detector.addDependenciesOfFile(psiFile, psiFile.getProject(), psiFile, null, null, false);

                            if (cycle != null)
                            {
                                DetectionResult cycleDetectionResult = detector.getCycleDetectionResult(cycle);
                                detector.updateIncriminatingModules(cycleDetectionResult.getDependencies(), cycleDetectionResult.getCyclePath());
                                count++;
                            }
                        }
                        catch(ProcessCanceledException exception)
                        {
                            new Notification("needsmoredojo", "Find Circular Dependencies", "Find Circular Dependencies action was canceled", NotificationType.INFORMATION).notify(e.getProject());
                            return;
                        }
                        catch (Exception ex)
                        {
                            logger.error(ex, ex);
                        }
                    }

                    if (count == 0)
                    {
                        new Notification("needsmoredojo", "Find Circular Dependencies", "No cycles were found in the dependency graph", NotificationType.INFORMATION).notify(e.getProject());
                    }
                    else
                    {
                        updateToolWindow(count, e.getProject(), detector);
                    }
                }
                catch(ProcessCanceledException exception)
                {
                    new Notification("needsmoredojo", "Find Circular Dependencies", "Find Circular Dependencies action was canceled", NotificationType.INFORMATION).notify(e.getProject());
                }
            }
        }, "Searching for circular dependencies", true, e.getProject());
    }
}
