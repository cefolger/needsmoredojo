package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.UnusedImportsDetector;
import com.intellij.codeInspection.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class UnusedImportsInspection extends LocalInspectionTool
{
    @Override
    public String getDisplayName()
    {
        return "Check for unused imports";
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return true;
    }

    @Override
    public String getShortName()
    {
        return "UnusedImportsInspection";
    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, boolean isOnTheFly)
    {
        DefineResolver resolver = new DefineResolver();
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        List<PsiElement> defines = new ArrayList<PsiElement>();
        final List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();

        resolver.gatherDefineAndParameters(file, defines, parameters);
        UnusedImportsDetector detector = new UnusedImportsDetector();
        file.accept(detector.getVisitorToRemoveUsedParameters(parameters, defines));

        LocalQuickFix fix = null;
        for(int i=0;i<parameters.size();i++)
        {
            PsiElement define =  null;

            if(i < defines.size())
            {
                define = defines.get(i);
            }

            PsiElement parameter = null;
            if(i < parameters.size())
            {
                parameter = parameters.get(i);
            }

            if (parameter != null)
            {
                descriptors.add(manager.createProblemDescriptor(parameter, String.format("Unused AMD import: %s", parameter.getText()), fix, ProblemHighlightType.LIKE_DEPRECATED, true));
            }

            if (define != null)
            {
                descriptors.add(manager.createProblemDescriptor(define, String.format("Unused AMD import: %s", define.getText()), fix, ProblemHighlightType.LIKE_DEPRECATED, true));
            }
        }

        return descriptors.toArray(new ProblemDescriptor[0]);
    }
}
