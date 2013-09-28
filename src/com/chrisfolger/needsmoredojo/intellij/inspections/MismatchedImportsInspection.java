package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.naming.MismatchedImportsDetector;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.codeInspection.*;
import com.intellij.openapi.components.ServiceManager;
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
        LocalQuickFix noFix = null;

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

            LocalQuickFix fix = noFix;
            if(define != null && parameter != null)
            {
                fix = new MismatchedImportsQuickFix(define, parameter);
            }

            // check if the previous import was also mismatched. If it was, it's possible that they were flipped by accident.
            // but exclude the case where there are three or more in a row, because then it's probably just that the two
            // lists are completely out of sync.
            boolean importsSwapped = false;
            if(i > 0)
            {
                boolean nextIsMismatched = false;
                if(i <= mismatches.size() - 2)
                {
                    MismatchedImportsDetector.Mismatch nextMismatch = mismatches.get(i+1);
                    if(nextMismatch.getIndex() == mismatch.getIndex() + 1)
                    {
                        nextIsMismatched = true;
                    }
                }

                MismatchedImportsDetector.Mismatch previousMismatch = mismatches.get(i-1);
                if(previousMismatch.getIndex() == mismatch.getIndex() - 1 && !nextIsMismatched)
                {
                    importsSwapped = true;
                }
            }

            LocalQuickFix importFix = null;
            if(importsSwapped)
            {
                importFix = new MismatchedImportsQuickFix(define, parameter);
            }

            if (parameter != null)
            {
                descriptors.add(manager.createProblemDescriptor(parameter, String.format("Mismatch between define %s and parameter %s", defineString, parameterString), true, ProblemHighlightType.ERROR, true, fix));
            }

            if (define != null)
            {
                if(importFix != null)
                {
                    descriptors.add(manager.createProblemDescriptor(define, String.format("foooo", defineString, parameterString), true, ProblemHighlightType.ERROR, true, fix, new MismatchedImportsQuickFix(parameter, define)));
                }
                else
                {
                    descriptors.add(manager.createProblemDescriptor(define, String.format("Mismatch between define %s and parameter %s", defineString, parameterString), true, ProblemHighlightType.ERROR, true, fix));
                }

            }
        }

        return descriptors.toArray(new ProblemDescriptor[0]);
    }
}
