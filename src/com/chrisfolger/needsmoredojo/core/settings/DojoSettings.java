package com.chrisfolger.needsmoredojo.core.settings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;


@State(
        name = "NeedsMoreDojoConfiguration",
        storages = {
                @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
                @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/needsmoredojoconfig.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class DojoSettings implements PersistentStateComponent<DojoSettings>
{
    public LinkedHashMap<String, String> amdImportNamingExceptions;
    public String dojoSourcesDirectory;
    public String projectSourcesDirectory;

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
        return dojoSourcesDirectory;
    }

    public void setDojoSourcesDirectory(Project project, String value)
    {
        dojoSourcesDirectory = value;
    }

    public String getProjectSourcesDirectory(Project project)
    {
        return projectSourcesDirectory;
    }

    public void setProjectSourcesDirectory(Project project, String value)
    {
        projectSourcesDirectory = value;
    }

    @Nullable
    @Override
    public DojoSettings getState() {
        return this;
    }

    @Override
    public void loadState(DojoSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
