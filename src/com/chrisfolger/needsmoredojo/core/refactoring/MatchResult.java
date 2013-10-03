package com.chrisfolger.needsmoredojo.core.refactoring;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.psi.PsiFile;

public class MatchResult
{
    private JSCallExpression callExpression;
    private int index;
    private String path;
    private char quote;
    private PsiFile module;
    private String pluginResourceId;
    private PsiFile pluginResourceFile;

    public MatchResult(PsiFile module, int index, String path, char quote, String pluginResourceId, PsiFile pluginResourceFile, JSCallExpression callExpression) {
        this.index = index;
        this.path = path;
        this.quote = quote;
        this.module = module;
        this.pluginResourceId = pluginResourceId;
        this.pluginResourceFile = pluginResourceFile;
        this.callExpression = callExpression;
    }

    public String getPluginResourceId() {
        return pluginResourceId;
    }

    public JSCallExpression getCallExpression() {
        return callExpression;
    }

    public PsiFile getPluginResourceFile() {
        return pluginResourceFile;
    }

    public PsiFile getModule() {
        return module;
    }

    public char getQuote() {
        return quote;
    }

    public int getIndex() {
        return index;
    }

    public String getPath() {
        return path;
    }
}