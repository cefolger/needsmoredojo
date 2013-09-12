package com.chrisfolger.needsmoredojo.core.amd.filesystem;

public class SourceLibrary
{
    private String name;
    private String path;
    private boolean canUseRelativePaths;

    public SourceLibrary(String name, String path, boolean canUseRelativePaths) {
        this.name = name;
        this.path = path;
        this.canUseRelativePaths = canUseRelativePaths; 
    }

    public boolean isCanUseRelativePaths() {
        return canUseRelativePaths;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
