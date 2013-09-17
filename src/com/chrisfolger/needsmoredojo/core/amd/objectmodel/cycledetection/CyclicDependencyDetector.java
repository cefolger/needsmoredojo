package com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.importing.UnusedImportsRemover;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.log4j.Logger;

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
            String unusedString = "";
            if(parent.isUnused())
            {
                unusedString = " (unused)";
            }
            path = parent.getFile().getName() + unusedString + " -> " + path;

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

    private boolean importInList(List<PsiElement> defines, String module)
    {
        for(PsiElement define : defines)
        {
            if(define.getText().replaceAll("'|\"", "").equals(module))
            {
                return true;
            }
        }
        return false;
    }

    public DependencyNode addDependenciesOfFile(PsiFile originalFile, Project project, PsiFile psiFile, DependencyNode parent, String modulePath, boolean unused)
    {
        if(parent == null)
        {
            dependencies = new HashSet<String>();
        }

        DependencyNode node = new DependencyNode(psiFile, parent, modulePath, unused);
        if(parent != null)
        {
            parent.add(node);
        }

        DefineResolver resolver = new DefineResolver();
        final List<PsiElement> parameters = new ArrayList<PsiElement>();
        final List<PsiElement> defines = new ArrayList<PsiElement>();

        resolver.gatherDefineAndParameters(psiFile, defines, parameters);

        /*
            get a list of unused defines and parameters

            this way we can note if a module dependency is unused when the path is displayed
        */
        List<PsiElement> unusedDefines = new ArrayList<PsiElement>();
        List<PsiElement> unusedParameters = new ArrayList<PsiElement>();
        unusedDefines.addAll(defines);
        unusedParameters.addAll(parameters);

        UnusedImportsRemover detector = new UnusedImportsRemover();
        psiFile.accept(detector.getVisitorToRemoveUsedModulesFromLists(unusedParameters, unusedDefines, ServiceManager.getService(psiFile.getProject(), DojoSettings.class).getRuiImportExceptions()));

        for(PsiElement element : defines)
        {
            String moduleName = element.getText().replaceAll("'|\"", "");
            String define = moduleName + ".js";

            // now open the file and find the reference in it
            VirtualFile htmlFile = SourcesLocator.getAMDImportFile(element.getProject(), define, psiFile.getContainingFile().getContainingDirectory());
            if(DojoModuleFileResolver.isDojoModule(moduleName))
            {
                continue;
            }

            if(htmlFile == null)
            {
                continue;
            }

            boolean isUnused = importInList(unusedDefines, moduleName);

            PsiFile templateFile = PsiManager.getInstance(psiFile.getProject()).findFile(htmlFile);

            if(templateFile == null)
            {
                Logger.getLogger(CyclicDependencyDetector.class).error("could not find module file: " + htmlFile.getCanonicalPath());
            }

            if(templateFile.getName().equals(originalFile.getName()))
            {
                DependencyNode original = new DependencyNode(originalFile, node, element.getText(), isUnused);
                node.add(original);
                return original;
            }

            if(dependencies.contains(templateFile.getName()))
            {
                continue;
            }

            dependencies.add(templateFile.getName());
            DependencyNode result = addDependenciesOfFile(originalFile, project, templateFile, node, element.getText(), isUnused);
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
