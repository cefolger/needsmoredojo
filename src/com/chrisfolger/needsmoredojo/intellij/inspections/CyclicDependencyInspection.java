package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CyclicDependencyInspection extends LocalInspectionTool
{
    private Map<String, List<String>> incriminatingModules = new HashMap<String, List<String>>();

    public Map<String, List<String>> getIncriminatingModules() {
        return incriminatingModules;
    }

    private class DependencyNode
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
    }

    private Set<String> dependencies;
    private PsiFile originalFile;

    @Override
    public String getDisplayName()
    {
        return "Check for cyclic dependencies in AMD modules";
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return true;
    }

    @Override
    public String getShortName()
    {
        return "CyclicDependencyInspection";
    }

    @Nullable    @Override
    public String getStaticDescription() {
        return "Detects AMD imports that have inconsistent naming between the module path and the variable name. " +
                "\n\nExample: \n\ndefine([\n    'dojo/foo'\n], function(someOtherName) {}); \n\nvs\n\n define([\n    'dojo/foo'\n'], function(foo) {});";
    }

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName()
    {
        return "Needs More Dojo";
    }

    @Override
    public String[] getGroupPath()
    {
        return new String[] { "JavaScript", "Needs More Dojo "};
    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, boolean isOnTheFly)
    {
        final List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();

        dependencies = new HashSet<String>();

        originalFile = file;

        DependencyNode cycle = addDependenciesOfFile(file.getProject(), file, dependencies, null, null);

        if(cycle != null)
        {
            DependencyNode lastDependency = null;

            String path = cycle.file.getName();

            DependencyNode parent = cycle.parent;
            Set<String> dependencies = new HashSet<String>();
            while(parent != null)
            {
                path = parent.file.getName() + " -> " + path;

                if(parent.parent != null && parent.parent.parent == null)
                {
                    lastDependency = parent;
                }

                if(parent != null && parent.modulePath != null)
                {
                    dependencies.add(NameResolver.getModuleName(parent.modulePath));
                }
                parent = parent.parent;
            }

            DefineResolver resolver = new DefineResolver();
            final List<PsiElement> parameters = new ArrayList<PsiElement>();
            final List<PsiElement> defines = new ArrayList<PsiElement>();
            resolver.gatherDefineAndParameters(file, defines, parameters);

            for(PsiElement define : defines)
            {
                if(define.getText().equals(lastDependency.modulePath))
                {
                    LocalQuickFix fix = null;
                  //  descriptors.add(manager.createProblemDescriptor(define, "A cyclic dependency exists with the path: \n" + path, fix, ProblemHighlightType.GENERIC_ERROR, true));
                }
            }

            if(!isOnTheFly)
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

        return descriptors.toArray(new ProblemDescriptor[0]);
    }

    private DependencyNode addDependenciesOfFile(Project project, PsiFile psiFile, Set<String> dependencies, DependencyNode parent, String modulePath)
    {
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
            DependencyNode result = addDependenciesOfFile(project, templateFile, dependencies, node, element.getText());
            if(result != null)
            {
                return result;
            }
        }

        return null;
    }
}