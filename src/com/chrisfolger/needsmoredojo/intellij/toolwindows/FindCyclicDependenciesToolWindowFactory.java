package com.chrisfolger.needsmoredojo.intellij.toolwindows;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

public class FindCyclicDependenciesToolWindowFactory implements ToolWindowFactory
{
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        toolWindow.setToHideOnEmptyContent(true);
        toolWindow.hide(null);
        toolWindow.setAvailable(false, null);
    }
}
