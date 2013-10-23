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
    private LinkedHashMap<String, String> ruiImportExceptions;
    private String dojoSourcesDirectory;
    private String projectSourcesDirectory;
    private boolean preferRelativeImports;
    private boolean dojoSourcesShareProjectSourcesRoot;
    private boolean needsMoreDojoEnabled;
    private boolean addModuleIfThereAreNoneDefined;
    private boolean allowCaseInsensitiveSearch;
    private boolean setupWarningDisabled;
    private String supportedFileTypes;

    public DojoSettings()
    {
        setupWarningDisabled = false;
        ruiImportExceptions = new LinkedHashMap<String, String>();
        ruiImportExceptions.put("dojox/form/Uploader/IFrame", "IFrame");
        ruiImportExceptions.put("dojox/form/Uploader/Flash", "Flash");
        ruiImportExceptions.put("dojox/form/Uploader", "Uploader");

        amdImportNamingExceptions = new LinkedHashMap<String, String>();
        amdImportNamingExceptions.put("dojo/sniff", "has");
        amdImportNamingExceptions.put("doh/main", "doh");
        preferRelativeImports = false;
        dojoSourcesShareProjectSourcesRoot = false;
        needsMoreDojoEnabled = true;
        addModuleIfThereAreNoneDefined = false;
        allowCaseInsensitiveSearch = true;
        supportedFileTypes = "jsp,js,php,html";
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

    public boolean isAllowCaseInsensitiveSearch() {
        return allowCaseInsensitiveSearch;
    }

    public void setAllowCaseInsensitiveSearch(boolean allowCaseInsensitiveSearch) {
        this.allowCaseInsensitiveSearch = allowCaseInsensitiveSearch;
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

    public LinkedHashMap<String, String> getRuiImportExceptions() {
        return ruiImportExceptions;
    }

    public void setRuiImportExceptions(LinkedHashMap<String, String> ruiImportExceptions) {
        this.ruiImportExceptions = ruiImportExceptions;
    }

    public boolean isDojoSourcesShareProjectSourcesRoot() {
        return dojoSourcesShareProjectSourcesRoot;
    }

    public boolean isAddModuleIfThereAreNoneDefined() {
        return addModuleIfThereAreNoneDefined;
    }

    public void setAddModuleIfThereAreNoneDefined(boolean addModuleIfThereAreNoneDefined) {
        this.addModuleIfThereAreNoneDefined = addModuleIfThereAreNoneDefined;
    }

    public void setDojoSourcesShareProjectSourcesRoot(boolean dojoSourcesShareProjectSourcesRoot) {
        this.dojoSourcesShareProjectSourcesRoot = dojoSourcesShareProjectSourcesRoot;
    }

    public String getSupportedFileTypes() {
        return supportedFileTypes;
    }

    public void setSupportedFileTypes(String supportedFileTypes) {
        this.supportedFileTypes = supportedFileTypes;
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

    public boolean isNeedsMoreDojoEnabled() {
        return needsMoreDojoEnabled;
    }

    public void setNeedsMoreDojoEnabled(boolean needsMoreDojoEnabled) {
        this.needsMoreDojoEnabled = needsMoreDojoEnabled;
    }

    public boolean isSetupWarningDisabled() {
        return setupWarningDisabled;
    }

    public void setSetupWarningDisabled(boolean setupWarningDisabled) {
        this.setupWarningDisabled = setupWarningDisabled;
    }
}
