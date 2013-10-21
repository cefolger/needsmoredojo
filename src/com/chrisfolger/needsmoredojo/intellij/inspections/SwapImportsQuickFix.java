package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.importing.ImportReorderer;
import com.chrisfolger.needsmoredojo.core.amd.naming.MismatchedImportsDetector;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * This quickfix will swap two AMD imports that were assumed to be mismatched accidentally.
 */
public class SwapImportsQuickFix implements LocalQuickFix {
    private MismatchedImportsDetector.Mismatch first;
    private MismatchedImportsDetector.Mismatch second;

    public SwapImportsQuickFix(MismatchedImportsDetector.Mismatch first, MismatchedImportsDetector.Mismatch second) {
        this.first = first;
        this.second = second;
    }

    @NotNull
    @Override
    public String getName() {
        return "Swap imports: " + first.getDefine().getText() + " with " + second.getDefine().getText();
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Needs More Dojo";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        ImportReorderer reorderer = new ImportReorderer();

                        reorderer.reorder(first.getDefine(), second.getDefine());
                    }
                });
            }
        },
        "Swap AMD Import",
        "Swap AMD Import");
    }
}
