package com.chrisfolger.needsmoredojo.core.settings;

import com.intellij.openapi.project.Project;

public class DojoSettings
{
    private static DojoSettings instance;

    public void setInstance(DojoSettings inst)
    {
        instance = inst;
    }

    public DojoSettings getInstance()
    {
        return instance;
    }

    public String getDojoSourcesDirectory(Project project)
    {
        return "";
    }
}
