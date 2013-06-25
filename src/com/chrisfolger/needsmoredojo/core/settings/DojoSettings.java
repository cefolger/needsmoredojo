package com.chrisfolger.needsmoredojo.core.settings;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

public class DojoSettings
{
    private static DojoSettings instance;

    public void setInstance(DojoSettings inst)
    {
        instance = inst;
    }

    public static DojoSettings getInstance()
    {
        if(instance == null)
        {
            instance = new DojoSettings();
        }

        return instance;
    }

    public String getDojoSourcesDirectory(Project project)
    {
        return PropertiesComponent.getInstance(project).getValue("com.chrisfolger.needsmoredojo.core.settings.dojosources", "");
    }

    public void setDojoSourcesDirectory(Project project, String value)
    {
        PropertiesComponent.getInstance(project).setValue("com.chrisfolger.needsmoredojo.core.settings.dojosources", value);
    }
}
