package com.chrisfolger.needsmoredojo.actions;

import com.chrisfolger.needsmoredojo.base.DefineResolver;
import com.chrisfolger.needsmoredojo.conventions.UnusedImportsDetector;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UnusedImportsAction extends AnAction {
    protected boolean deleteMode = false;

    public void actionPerformed(@NotNull final AnActionEvent e)
    {
        final PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);

        DefineResolver resolver = new DefineResolver();
        final List<PsiElement> parameters = new ArrayList<PsiElement>();
        final List<PsiElement> defines = new ArrayList<PsiElement>();
        resolver.gatherDefineAndParameters(psiFile, defines, parameters);

        UnusedImportsDetector detector = new UnusedImportsDetector();
        psiFile.accept(detector.getVisitorToRemoveUsedParameters(parameters, defines));

        highlightElement(e.getProject(), parameters.toArray(new PsiElement[0]));
        highlightElement(e.getProject(), defines.toArray(new PsiElement[0]));

        if(this.deleteMode)
        {
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
                                elementsToDelete.add(element.getNextSibling());
                            }
                            for(PsiElement element : defines)
                            {
                                elementsToDelete.add(element);
                                if(results.toString().equals(""))
                                {
                                    results.append(element.getText());
                                }
                                else
                                {
                                    results.append("," + element.getText());
                                }
                                elementsToDelete.add(element.getNextSibling());
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
                                Notifications.Bus.notify(new Notification("needsmoredojo", "Deleted Imports", results.toString(), NotificationType.INFORMATION));
                            }
                            else
                            {
                                Notifications.Bus.notify(new Notification("needsmoredojo", "No unused imports", "No unused imports were detected to delete", NotificationType.INFORMATION));
                            }
                        }
                    });
                }
            },
            "Delete unused AMD imports",
            "Delete unused AMD imports");

        }
    }

    private void highlightElement(@NotNull Project project, @NotNull PsiElement[] elements)
    {
        final FileEditorManager editorManager =
                FileEditorManager.getInstance(project);
        final HighlightManager highlightManager =
                HighlightManager.getInstance(project);
        final EditorColorsManager editorColorsManager =
                EditorColorsManager.getInstance();
        final Editor editor = editorManager.getSelectedTextEditor();
        final EditorColorsScheme globalScheme =
                editorColorsManager.getGlobalScheme();
        final TextAttributes textattributes =
                globalScheme.getAttributes(
                        EditorColors.SEARCH_RESULT_ATTRIBUTES);

        highlightManager.addOccurrenceHighlights(
                editor, elements, textattributes, true, null);
        final WindowManager windowManager = WindowManager.getInstance();
        final StatusBar statusBar = windowManager.getStatusBar(project);
        statusBar.setInfo("Press Esc to remove highlighting");
    }
}
