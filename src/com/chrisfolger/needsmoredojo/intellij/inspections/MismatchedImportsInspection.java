package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.importing.InvalidDefineException;
import com.chrisfolger.needsmoredojo.core.amd.naming.MismatchedImportsDetector;
import com.chrisfolger.needsmoredojo.core.amd.naming.MismatchedImportsDetectorCache;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class MismatchedImportsInspection extends DojoInspection
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

    @NotNull
    @Override
    public String[] getGroupPath()
    {
        return new String[] { "JavaScript", "Needs More Dojo "};
    }

    private void addProblemsForBlock(JSCallExpression expression, List<ProblemDescriptor> descriptors, PsiFile file, InspectionManager manager)
    {
        List<PsiElement> blockDefines = new ArrayList<PsiElement>();
        List<PsiElement> blockParameters = new ArrayList<PsiElement>();

        try {
            new DefineResolver().addDefinesAndParametersOfImportBlock(expression, blockDefines, blockParameters);
        } catch (InvalidDefineException e) {

        }

        Map<String, Integer> parameterOccurrences = new HashMap<String, Integer>();
        for(PsiElement parameter : blockParameters)
        {
            if(parameter == null) continue;
            if(parameterOccurrences.containsKey(parameter.getText()))
            {
                parameterOccurrences.put(parameter.getText(), parameterOccurrences.get(parameter.getText()) + 1);
            }
            else
            {
                parameterOccurrences.put(parameter.getText(), 1);
            }
        }

        LocalQuickFix noFix = null;
        List<MismatchedImportsDetector.Mismatch> mismatches = new MismatchedImportsDetector().matchOnList(blockDefines.toArray(new PsiElement[0]),
                blockParameters.toArray(new PsiElement[0]),
                ServiceManager.getService(file.getProject(), DojoSettings.class).getNamingExceptionList(),
                ServiceManager.getService(file.getProject(), DojoSettings.class),
                ServiceManager.getService(file.getProject(), MismatchedImportsDetectorCache.class));
        for(int i=0;i<mismatches.size();i++)
        {
            MismatchedImportsDetector.Mismatch mismatch = mismatches.get(i);
            PsiElement define = mismatch.getDefine();
            PsiElement parameter = mismatch.getParameter();

            if(define != null && parameter != null && !(define instanceof JSLiteralExpression))
            {
                // this is to account for expressions in the define/require array literal. They are perfectly valid,
                // so we can't flag them as mismatched
                continue;
            }

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
            LocalQuickFix exceptionFix = null;

            if(define != null && parameter != null)
            {
                String normalName = NameResolver.defineToParameter(define.getText(), ServiceManager.getService(define.getProject(), DojoSettings.class).getNamingExceptionList());
                if(parameterOccurrences.containsKey(normalName))
                {
                    fix = new MismatchedImportsQuickFix(define, parameter, mismatch.getAbsolutePath());
                }
                else
                {
                    fix = new MismatchedImportsQuickFix(define, parameter, null);
                }
                exceptionFix = new AddExceptionQuickFix(define, parameter, mismatch.getAbsolutePath());
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

            if (parameter != null)
            {
                descriptors.add(manager.createProblemDescriptor(parameter, String.format("Mismatch between define %s and parameter %s", defineString, parameterString), true, ProblemHighlightType.ERROR, true, fix, exceptionFix));
            }

            if (define != null)
            {
                descriptors.add(manager.createProblemDescriptor(define, String.format("Mismatch between define %s and parameter %s", defineString, parameterString), true, ProblemHighlightType.ERROR, true, fix, exceptionFix));
            }

            if(importsSwapped)
            {
                SwapImportsQuickFix importFix = new SwapImportsQuickFix(mismatch, mismatches.get(i-1));
                descriptors.addAll(addQuickFixToOtherMismatch(mismatch, mismatches.get(i - 1), importFix, manager));
                descriptors.addAll(addQuickFixToOtherMismatch(mismatches.get(i - 1), mismatch, importFix, manager));
            }
        }
    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, boolean isOnTheFly)
    {
        if(!isEnabled(file.getProject()))
        {
            return new ProblemDescriptor[0];
        }

        DefineResolver resolver = new DefineResolver();
        final List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();

        Set<JSCallExpression> expressions = resolver.getAllImportBlocks(file);
        for(JSCallExpression expression : expressions)
        {
            addProblemsForBlock(expression, descriptors, file, manager);
        }

        return descriptors.toArray(new ProblemDescriptor[0]);
    }

    private List<ProblemDescriptor> addQuickFixToOtherMismatch(MismatchedImportsDetector.Mismatch mismatch, MismatchedImportsDetector.Mismatch secondMismatch,  SwapImportsQuickFix quickFix, InspectionManager manager)
    {
        List<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();

        if(mismatch.getDefine() == null || mismatch.getParameter() == null)
        {
            return descriptors;
        }

        descriptors.add(manager.createProblemDescriptor(mismatch.getDefine(), String.format("Potentially swapped imports: %s and %s", mismatch.getDefine().getText(), secondMismatch.getDefine().getText()), true, ProblemHighlightType.ERROR, true, quickFix));
        descriptors.add(manager.createProblemDescriptor(mismatch.getParameter(), String.format("Potentially swapped imports: %s and %s", mismatch.getDefine().getText(), secondMismatch.getDefine().getText()), true, ProblemHighlightType.ERROR, true, quickFix));

        return descriptors;
    }
}
