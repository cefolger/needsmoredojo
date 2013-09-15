package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.CyclicDependencyDetector;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.DependencyNode;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.cycledetection.DetectionResult;
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
    private CyclicDependencyDetector detector;

    @Override
    public String getDisplayName()
    {
        return "Check for cyclic dependencies in AMD modules";
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return false;
    }

    @Override
    public String getShortName()
    {
        return "CyclicDependencyInspection";
    }

    @Nullable    @Override
    public String getStaticDescription() {
        return "Detects potential cycles on the current file. Cyclic dependencies in dojo can cause obscure bugs if not accounted for. This inspection is "
                + "disabled by default.";
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
        if(detector == null || isOnTheFly)
        {
            detector = new CyclicDependencyDetector();
        }

        final List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();

        DependencyNode cycle = detector.addDependenciesOfFile(file, file.getProject(), file, null, null);

        if(cycle != null)
        {
            DetectionResult cycleDetectionResult = detector.getCycleDetectionResult(cycle);

            DefineResolver resolver = new DefineResolver();
            final List<PsiElement> parameters = new ArrayList<PsiElement>();
            final List<PsiElement> defines = new ArrayList<PsiElement>();
            resolver.gatherDefineAndParameters(file, defines, parameters);

            for(PsiElement define : defines)
            {
                if(define.getText().equals(cycleDetectionResult.getLastDependency().getModulePath()))
                {
                    LocalQuickFix fix = null;
                    descriptors.add(manager.createProblemDescriptor(define, "A cyclic dependency exists with the path: \n" + cycleDetectionResult.getCyclePath(), fix, ProblemHighlightType.GENERIC_ERROR, true));
                }
            }
        }

        return descriptors.toArray(new ProblemDescriptor[0]);
    }
}