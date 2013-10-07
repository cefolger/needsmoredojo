package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

public class DojoInspection extends LocalInspectionTool
{
    protected boolean isEnabled(Project project)
    {
        DojoSettings settings = ServiceManager.getService(project, DojoSettings.class);
        return settings.isNeedsMoreDojoEnabled();
    }
}
