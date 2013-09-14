package com.chrisfolger.needsmoredojo.core.amd.define.organizer;

public class SortingResult
{
    private SortedPsiElementAdapter[] defines;
    private SortedPsiElementAdapter[] parameters;
    private int singleQuotes;
    private int doubleQuotes;

    public SortingResult(SortedPsiElementAdapter[] defines, SortedPsiElementAdapter[] parameters)
    {
        this.defines = defines;
        this.parameters = parameters;
    }

    public SortedPsiElementAdapter[] getParameters() {
        return parameters;
    }

    public void setParameters(SortedPsiElementAdapter[] parameters) {
        this.parameters = parameters;
    }

    public SortedPsiElementAdapter[] getDefines() {
        return defines;
    }

    public void setDefines(SortedPsiElementAdapter[] defines) {
        this.defines = defines;
    }

    public int getSingleQuotes() {
        return singleQuotes;
    }

    public void setSingleQuotes(int singleQuotes) {
        this.singleQuotes = singleQuotes;
    }

    public int getDoubleQuotes() {
        return doubleQuotes;
    }

    public void setDoubleQuotes(int doubleQuotes) {
        this.doubleQuotes = doubleQuotes;
    }
}