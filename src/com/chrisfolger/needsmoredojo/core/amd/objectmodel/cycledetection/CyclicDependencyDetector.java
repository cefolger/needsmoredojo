package com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.util.*;

public class CyclicDependencyDetector
{
    private Map<String, List<String>> incriminatingModules;
    private Set<String> dependencies;

    public CyclicDependencyDetector()
    {
        incriminatingModules = new HashMap<String, List<String>>();
        dependencies = new HashSet<String>();
    }

    public Map<String, List<String>> getIncriminatingModules() {
        return incriminatingModules;
    }

    public DetectionResult getCycleDetectionResult(DependencyNode root)
    {
        DependencyNode lastDependency = null;

        String path = root.getFile().getName();

        DependencyNode parent = root.getParent();
        Set<String> dependencies = new HashSet<String>();
        while(parent != null)
        {
            path = parent.getFile().getName() + " -> " + path;

            if(parent.getParent() != null && parent.getParent().getParent() == null)
            {
                lastDependency = parent;
            }

            if(parent != null && parent.getModulePath() != null)
            {
                dependencies.add(NameResolver.getModuleName(parent.getModulePath()));
            }
            parent = parent.getParent();
        }

        return new DetectionResult(path, lastDependency, dependencies);
    }

    public DependencyNode addDependenciesOfFile(PsiFile originalFile, Project project, PsiFile psiFile, DependencyNode parent, String modulePath)
    {
        if(parent == null)
        {
            dependencies = new HashSet<String>();
        }

        DependencyNode node = new DependencyNode(psiFile, parent, modulePath);
        if(parent != null)
        {
            parent.add(node);
        }

        DefineResolver resolver = new DefineResolver();
        final List<PsiElement> parameters = new ArrayList<PsiElement>();
        final List<PsiElement> defines = new ArrayList<PsiElement>();

        resolver.gatherDefineAndParameters(psiFile, defines, parameters);

        for(PsiElement element : defines)
        {
            String define = element.getText().replaceAll("'", "").replaceAll("\"", "") + ".js";

            // TODO exclude dojo sources when they are referenced relatively
            // TODO the correct way to do this is probably through ModuleImporter
            // now open the file and find the reference in it
            VirtualFile htmlFile = SourcesLocator.getAMDImportFile(element.getProject(), define, psiFile.getContainingFile().getContainingDirectory());

            if(DojoModuleFileResolver.isDojoModule(element.getText().replaceAll("'", "").replaceAll("\"", "")))
            {
                continue;
            }

            if(htmlFile == null)
            {
                continue;
            }

            PsiFile templateFile = PsiManager.getInstance(psiFile.getProject()).findFile(htmlFile);
            if(templateFile.getName().equals(originalFile.getName()))
            {
                DependencyNode original = new DependencyNode(originalFile, node, element.getText());
                node.add(original);
                return original;
            }

            if(dependencies.contains(templateFile.getName()))
            {
                continue;
            }

            dependencies.add(templateFile.getName());
            DependencyNode result = addDependenciesOfFile(originalFile, project, templateFile, node, element.getText());
            if(result != null)
            {
                return result;
            }
        }

        return null;
    }

    public void updateIncriminatingModules(Set<String> dependencies, String path)
    {
        for(String module : dependencies)
        {
            if(incriminatingModules.containsKey(module))
            {
                incriminatingModules.get(module).add(path);
            }
            else
            {
                incriminatingModules.put(module, new ArrayList<String>());
                incriminatingModules.get(module).add(path);
            }
        }
    }
}
