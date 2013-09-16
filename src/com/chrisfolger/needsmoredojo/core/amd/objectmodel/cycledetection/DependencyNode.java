package com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection;

import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;

public class DependencyNode
{
    private List<DependencyNode> nodes;
    private DependencyNode parent;
    private PsiFile file;
    private String modulePath;
    private boolean unused;

    public DependencyNode(PsiFile file, DependencyNode parent, String modulePath, boolean unused)
    {
        nodes = new ArrayList<DependencyNode>();
        this.parent = parent;
        this.file = file;
        this.modulePath = modulePath;
        this.unused = unused;
    }

    public DependencyNode(PsiFile file, DependencyNode parent, String modulePath)
    {
        this(file, parent, modulePath, false);
    }

    public void setUnused(boolean unused) {
        this.unused = unused;
    }

    public boolean isUnused() {
        return unused;
    }

    public void add(DependencyNode node)
    {
        nodes.add(node);
    }

    public List<DependencyNode> getNodes() {
        return nodes;
    }

    public DependencyNode getParent() {
        return parent;
    }

    public PsiFile getFile() {
        return file;
    }

    public String getModulePath() {
        return modulePath;
    }
}
