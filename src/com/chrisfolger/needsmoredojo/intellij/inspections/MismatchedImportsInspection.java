package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.MismatchedImportsDetector;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.codeInspection.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.graph.algo.Groups;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class MismatchedImportsInspection extends LocalInspectionTool
{
    @Override
    public String getDisplayName()
    {
        return "Check for inconsistently named imports";
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

    @Nullable
    @Override
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
        DefineResolver resolver = new DefineResolver();
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        List<PsiElement> defines = new ArrayList<PsiElement>();
        final List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();

        resolver.gatherDefineAndParameters(file, defines, parameters);

        LocalQuickFix fix = null;
        List<MismatchedImportsDetector.Mismatch> mismatches = new MismatchedImportsDetector().matchOnList(defines.toArray(new PsiElement[0]), parameters.toArray(new PsiElement[0]), ServiceManager.getService(file.getProject(), DojoSettings.class).getExceptionsMap());
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
