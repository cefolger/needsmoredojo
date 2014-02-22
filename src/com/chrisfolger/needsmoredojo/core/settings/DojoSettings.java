package com.chrisfolger.needsmoredojo.core.settings;

import com.chrisfolger.needsmoredojo.core.amd.naming.NameException;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@State(
        name = "NeedsMoreDojoConfiguration",
        storages = {
                @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
                @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/needsmoredojoconfig.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class DojoSettings implements PersistentStateComponent<DojoSettings>
{
    private static final String CURRENT_VERSION = "0.7";
    private LinkedHashMap<String, String> amdImportNamingExceptions;
    private LinkedHashMap<String, String> ruiImportExceptions;
    private List<String> amdImportNamingExceptionsList;

    private String dojoSourcesDirectory;
    private String projectSourcesDirectory;
    private boolean preferRelativeImports;
    private boolean dojoSourcesShareProjectSourcesRoot;
    private boolean needsMoreDojoEnabled;
    private boolean addModuleIfThereAreNoneDefined;
    private boolean allowCaseInsensitiveSearch;
    private boolean setupWarningDisabled;
    private boolean refactoringEnabled;
    private String supportedFileTypes;
    private boolean singleQuotedModuleIDs;
    // this will be used for converting to module specific sources later
    private String version;

    public DojoSettings()
    {
        setupWarningDisabled = false;
        ruiImportExceptions = new LinkedHashMap<String, String>();
        ruiImportExceptions.put("dojox/form/Uploader/IFrame", "IFrame");
        ruiImportExceptions.put("dojox/form/Uploader/Flash", "Flash");
        ruiImportExceptions.put("dojox/form/Uploader", "Uploader");
        amdImportNamingExceptionsList = new ArrayList<String>();

        refactoringEnabled = false;
        amdImportNamingExceptions = new LinkedHashMap<String, String>();
        amdImportNamingExceptions.put("dojo/sniff", "has");
        amdImportNamingExceptions.put("doh/main", "doh");
        preferRelativeImports = false;
        dojoSourcesShareProjectSourcesRoot = false;
        needsMoreDojoEnabled = true;
        addModuleIfThereAreNoneDefined = false;
        allowCaseInsensitiveSearch = true;
        supportedFileTypes = "jsp,js,php,html";
        singleQuotedModuleIDs = true;
    }

    @Deprecated
    public @NotNull LinkedHashMap<String, String> getExceptionsMap()
    {
        return amdImportNamingExceptions;
    }

    @Deprecated
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

    @Deprecated
    public LinkedHashMap<String, String> getAmdImportNamingExceptions() {
        return amdImportNamingExceptions;
    }

    public boolean isAllowCaseInsensitiveSearch() {
        return allowCaseInsensitiveSearch;
    }

    public void setAllowCaseInsensitiveSearch(boolean allowCaseInsensitiveSearch) {
        this.allowCaseInsensitiveSearch = allowCaseInsensitiveSearch;
    }

    @Deprecated
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isRefactoringEnabled() {
        return refactoringEnabled;
    }

    public void setRefactoringEnabled(boolean refactoringEnabled) {
        this.refactoringEnabled = refactoringEnabled;
    }

    public  boolean isSingleQuotedModuleIDs() {
        return singleQuotedModuleIDs;
    }

    public void setSingleQuotedModuleIDs(boolean aSingleQuotedModuleIDs) {
        singleQuotedModuleIDs = aSingleQuotedModuleIDs;
    }

    public String upgrade()
    {
        String fromVersion = "";
        if(version != null)
        {
            fromVersion = " from " + version;
        }

        // any version 0.6 and earlier
        if(version == null || version.equals("0.6"))
        {
            amdImportNamingExceptions = new LinkedHashMap<String, String>();
            for(final Map.Entry<String, String> entry : amdImportNamingExceptions.entrySet())
            {
                amdImportNamingExceptionsList.add(entry.getKey() + "(" + entry.getValue());
            }


            version = CURRENT_VERSION;
            return "Needs More Dojo has upgraded your settings " + fromVersion + " to version " + CURRENT_VERSION;
        }

        return null;
    }

    public List<String> getAmdImportNamingExceptionsList() {
        return amdImportNamingExceptionsList;
    }

    public void setAmdImportNamingExceptionsList(List<String> amdImportNamingExceptionsList) {
        this.amdImportNamingExceptionsList = amdImportNamingExceptionsList;
    }

    public List<NameException> getNamingExceptionList()
    {
        List<NameException> results = new ArrayList<NameException>();
        for(String entry : amdImportNamingExceptionsList)
        {
            String[] items = entry.split("\\(");
            results.add(new NameException(items[0], items[1]));
        }

        return results;
    }
}
