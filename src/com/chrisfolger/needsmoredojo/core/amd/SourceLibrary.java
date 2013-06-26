package com.chrisfolger.needsmoredojo.core.amd;

public class SourceLibrary
{
    private String name;
    private String path;

    public SourceLibrary(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
