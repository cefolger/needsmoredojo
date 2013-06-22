package com.chrisfolger.needsmoredojo.core.settings;

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

    public String[] getSourcePathsRelativeToDojo()
    {
        return new String[] { "../"};
    }
}
