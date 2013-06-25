package com.chrisfolger.needsmoredojo.core.settings;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DojoSettings
{
    private static DojoSettings instance;

    public void setInstance(DojoSettings inst)
    {
        instance = inst;
    }

    private LinkedHashMap<String, String> amdImportNamingExceptions;

    public static DojoSettings getInstance()
    {
        if(instance == null)
        {
            instance = new DojoSettings();
        }

        return instance;
    }

    public DojoSettings()
    {
        // in the future, the user will be able to add their own exceptions
        // for now though, they are just hard-coded.
        amdImportNamingExceptions = new LinkedHashMap<String, String>();
        amdImportNamingExceptions.put("dojo/sniff", "has");
    }

    public @NotNull LinkedHashMap<String, String> getExceptionsMap()
    {
        return amdImportNamingExceptions;
    }

    public @Nullable String getException(@NotNull String module)
    {
        if(amdImportNamingExceptions.containsKey(module))
        {
            return amdImportNamingExceptions.get(module);
        }
        else
        {
            return null;
        }
    }

    public String getDojoSourcesDirectory(Project project)
    {
        return PropertiesComponent.getInstance(project).getValue("com.chrisfolger.needsmoredojo.core.settings.dojosources", "");
    }

    public void setDojoSourcesDirectory(Project project, String value)
    {
        PropertiesComponent.getInstance(project).setValue("com.chrisfolger.needsmoredojo.core.settings.dojosources", value);
    }

    public String getProjectSourcesDirectory(Project project)
    {
        return PropertiesComponent.getInstance(project).getValue("com.chrisfolger.needsmoredojo.core.settings.projectsources", "");
    }

    public void setProjectSourcesDirectory(Project project, String value)
    {
        PropertiesComponent.getInstance(project).setValue("com.chrisfolger.needsmoredojo.core.settings.projectsources", value);
    }
}
