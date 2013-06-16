package com.chrisfolger.needsmoredojo.inspections;

import com.chrisfolger.needsmoredojo.base.DefineResolver;
import com.chrisfolger.needsmoredojo.conventions.MismatchedImportsDetector;
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


public class MismatchedImportsInspection extends BaseLocalInspectionTool
{
    @Override
    public String getDisplayName()
    {
        return "Check for mismatched imports";
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return true;
    }

    @Override
    public String getShortName()
    {
        return "MismatchedImportsInspection";
    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, boolean isOnTheFly)
    {
        DefineResolver resolver = new DefineResolver();
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        List<PsiElement> defines = new ArrayList<PsiElement>();
        final List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();

        resolver.gatherDefineAndParameters(file, defines, parameters);

        LocalQuickFix fix = null;
        List<MismatchedImportsDetector.Mismatch> mismatches = new MismatchedImportsDetector().matchOnList(defines.toArray(new PsiElement[0]), parameters.toArray(new PsiElement[0]));
        for(int i=0;i<mismatches.size();i++)
        {
            MismatchedImportsDetector.Mismatch mismatch = mismatches.get(i);
            PsiElement define = mismatch.getDefine();
            PsiElement parameter = mismatch.getParameter();

            String defineString = "<no string>";
            String parameterString = "<no string>";

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
                descriptors.add(manager.createProblemDescriptor(parameter, String.format("Mismatch between define %s and parameter %s", defineString, parameterString), fix, ProblemHighlightType.ERROR, true));
            }

            if (define != null)
            {
                descriptors.add(manager.createProblemDescriptor(define, String.format("Mismatch between define %s and parameter %s", defineString, parameterString), fix, ProblemHighlightType.ERROR, true));
            }
        }

        return descriptors.toArray(new ProblemDescriptor[0]);
    }
}
