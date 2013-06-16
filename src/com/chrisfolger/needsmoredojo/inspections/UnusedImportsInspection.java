package com.chrisfolger.needsmoredojo.inspections;

import com.chrisfolger.needsmoredojo.base.DefineResolver;
import com.chrisfolger.needsmoredojo.conventions.UnusedImportsDetector;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ex.BaseLocalInspectionTool;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class UnusedImportsInspection extends BaseLocalInspectionTool
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

            String defineString = "";
            String parameterString = "";

            if(define != null)
            {
                defineString = define.getText();
            }

            if(parameter != null)
            {
                parameterString = parameter.getText();
            }

            if (parameter != null)
            {
                descriptors.add(manager.createProblemDescriptor(parameter, String.format("test", defineString, parameterString), fix, ProblemHighlightType.WEAK_WARNING, true));
            }

            if (define != null)
            {
                descriptors.add(manager.createProblemDescriptor(define, String.format("test", defineString, parameterString), fix, ProblemHighlightType.ERROR, true));
            }
        }

        return descriptors.toArray(new ProblemDescriptor[0]);
    }
}
