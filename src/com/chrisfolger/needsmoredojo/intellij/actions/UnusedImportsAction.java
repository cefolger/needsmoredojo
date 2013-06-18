package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.chrisfolger.needsmoredojo.core.amd.UnusedImportsDetector;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UnusedImportsAction extends AnAction {
    protected boolean deleteMode = false;

    private PsiElement getNearestComma(PsiElement start)
    {
        PsiElement sibling = start.getPrevSibling();
        while(sibling != null)
        {
            if(sibling.getText().equals(","))
            {
                return sibling;
            }

            sibling = sibling.getPrevSibling();
        }

        return null;
    }

    public void actionPerformed(@NotNull final AnActionEvent e)
    {
        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());

        DefineResolver resolver = new DefineResolver();
        final List<PsiElement> parameters = new ArrayList<PsiElement>();
        final List<PsiElement> defines = new ArrayList<PsiElement>();
        resolver.gatherDefineAndParameters(psiFile, defines, parameters);

        UnusedImportsDetector detector = new UnusedImportsDetector();
        psiFile.accept(detector.getVisitorToRemoveUsedParameters(parameters, defines));

        if(this.deleteMode)
        {
            // TODO refactor this please
            CommandProcessor.getInstance().executeCommand(psiFile.getProject(), new Runnable() {
                @Override
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            final StringBuilder results = new StringBuilder();
                            List<PsiElement> elementsToDelete = new ArrayList<PsiElement>();

                            for(PsiElement element : parameters)
                            {
                                elementsToDelete.add(element);

                                PsiElement nextSibling = element.getNextSibling();

                                // only remove commas at the end
                                if(nextSibling != null && nextSibling.getText().equals(","))
                                {
                                    elementsToDelete.add(element.getNextSibling());
                                }
                            }

                            int current = 0;
                            for(PsiElement element : defines)
                            {
                                elementsToDelete.add(element);
                                if(results.toString().equals("") && current < 4)
                                {
                                    results.append(element.getText());
                                }
                                else if (current < 4)
                                {
                                    results.append("," + element.getText());
                                }
                                else if (current == 4)
                                {
                                    results.append(String.format(" ... (+%d more) ", defines.size() - 4));
                                }
                                current++;

                                // special case for when the element we're removing is last on the list
                                PsiElement sibling = element.getNextSibling();
                                if(sibling != null && (sibling instanceof PsiWhiteSpace || sibling.getText().equals("]")))
                                {
                                    getNearestComma(sibling).delete();
                                }

                                // only remove the next sibling if it's a comma
                                PsiElement nextSibling = element.getNextSibling();
                                if(nextSibling != null && !nextSibling.getText().equals("]"))
                                {
                                    elementsToDelete.add(element.getNextSibling());
                                }
                            }

                            for(PsiElement element : elementsToDelete)
                            {
                                try
                                {
                                    element.delete();
                                }
                                catch(Exception e)
                                {

                                }
                            }

                            if(elementsToDelete.size() > 0)
                            {
                                Notifications.Bus.notify(new Notification("needsmoredojo", "Remove Unused Imports", results.toString(), NotificationType.INFORMATION));
                            }
                            else
                            {
                                Notifications.Bus.notify(new Notification("needsmoredojo", "Remove Unused Imports", "No unused imports were detected to delete", NotificationType.INFORMATION));
                            }
                        }
                    });
                }
            },
            "Remove Unused Imports",
            "Remove Unused Imports");

        }
    }
}
