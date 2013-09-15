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

    public DependencyNode(PsiFile file, DependencyNode parent, String modulePath)
    {
        nodes = new ArrayList<DependencyNode>();
        this.parent = parent;
        this.file = file;
        this.modulePath = modulePath;
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
