package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.importing.UnusedImportBlockEntry;
import com.chrisfolger.needsmoredojo.core.amd.importing.UnusedImportsRemover;
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


public class UnusedImportsInspection extends LocalInspectionTool
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
        DefineResolver resolver = new DefineResolver();
        final List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();

        UnusedImportsRemover detector = new UnusedImportsRemover();
        List<UnusedImportBlockEntry> results = detector.filterUsedModules(file, ServiceManager.getService(file.getProject(), DojoSettings.class).getRuiImportExceptions());


        for(UnusedImportBlockEntry result : results)
        {
            List<PsiElement> defines = result.getDefines();
            List<PsiElement> parameters = result.getParameters();

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
        }


        return descriptors.toArray(new ProblemDescriptor[0]);
    }
}
