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
    private LinkedHashMap<String, String> amdImportNamingExceptions;
    private String dojoSourcesDirectory;
    private String projectSourcesDirectory;
    private boolean preferRelativeImports;

    public DojoSettings()
    {
        // in the future, the user will be able to add their own exceptions
        // for now though, they are just hard-coded.
        amdImportNamingExceptions = new LinkedHashMap<String, String>();
        amdImportNamingExceptions.put("dojo/sniff", "has");
        preferRelativeImports = false;
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

    public LinkedHashMap<String, String> getAmdImportNamingExceptions() {
        return amdImportNamingExceptions;
    }

    public void setAmdImportNamingExceptions(LinkedHashMap<String, String> amdImportNamingExceptions) {
        this.amdImportNamingExceptions = amdImportNamingExceptions;
    }

    public String getDojoSourcesDirectory() {
        return dojoSourcesDirectory;
    }

    public void setDojoSourcesDirectory(String dojoSourcesDirectory) {
        this.dojoSourcesDirectory = dojoSourcesDirectory;
    }

    public String getProjectSourcesDirectory() {
        return projectSourcesDirectory;
    }

    public void setProjectSourcesDirectory(String projectSourcesDirectory) {
        this.projectSourcesDirectory = projectSourcesDirectory;
    }

    public boolean isPreferRelativeImports() {
        return preferRelativeImports;
    }

    public void setPreferRelativeImports(boolean preferRelativeImports) {
        this.preferRelativeImports = preferRelativeImports;
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
