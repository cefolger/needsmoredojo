package com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection;

import java.util.Set;

public class DetectionResult
{
    private String cyclePath;
    private DependencyNode lastDependency;
    private Set<String> dependencies;

    public DetectionResult(String cyclePath, DependencyNode lastDependency, Set<String> dependencies) {
        this.cyclePath = cyclePath;
        this.lastDependency = lastDependency;
        this.dependencies = dependencies;
    }

    public String getCyclePath() {
        return cyclePath;
    }

    public DependencyNode getLastDependency() {
        return lastDependency;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }
}
