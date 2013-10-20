package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.importing.UnusedImportBlockEntry;
import com.chrisfolger.needsmoredojo.core.amd.importing.UnusedImportsRemover;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class UnusedImportsInspection extends DojoInspection
{
    @Override
    public String getDisplayName()
    {
        return "Check for unused imports";
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

    @Nullable
    @Override
    public String getStaticDescription() {
        return "Detects AMD imports that are flagged as unused and marks them with a strikethrough";
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
        if(!isEnabled(file.getProject()))
        {
            return new ProblemDescriptor[0];
        }

        final List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();

        UnusedImportsRemover detector = new UnusedImportsRemover();
        List<UnusedImportBlockEntry> results = detector.filterUsedModules(file, ServiceManager.getService(file.getProject(), DojoSettings.class).getRuiImportExceptions());

        for(UnusedImportBlockEntry result : results)
        {
            List<PsiElement> defines = result.getDefines();
            List<PsiElement> parameters = result.getParameters();

            LocalQuickFix[] fixes = new LocalQuickFix[0];
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

                if(parameter != null && define != null)
                {
                    fixes = new LocalQuickFix[] { new RemoveImportQuickFix(define, parameter), new IgnoreImportQuickFix(define, parameter), new RemoveUnusedImportsQuickFix(define, parameter)};
                }
                else
                {
                    fixes = new LocalQuickFix[0];
                }

                if (parameter != null)
                {
                    descriptors.add(manager.createProblemDescriptor(parameter, String.format("Unused AMD import: %s", parameter.getText()), fixes, ProblemHighlightType.LIKE_DEPRECATED, true, false));
                }

                if (define != null)
                {
                    descriptors.add(manager.createProblemDescriptor(define, String.format("Unused AMD import: %s", define.getText()), fixes, ProblemHighlightType.LIKE_DEPRECATED, true, false));
                }
            }
        }


        return descriptors.toArray(new ProblemDescriptor[0]);
    }
}
