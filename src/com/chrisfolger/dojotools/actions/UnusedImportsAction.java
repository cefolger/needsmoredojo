package com.chrisfolger.dojotools.actions;

import com.chrisfolger.dojotools.base.DefineResolver;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.chrisfolger.dojotools.conventions.UnusedImportsDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.chrisfolger.dojotools.ui.DojoImportToolWindow;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Chris
 * Date: 12/20/12
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class UnusedImportsAction extends AnAction {
    @Nullable
    private static Map<String, String> domMappings = null;
    protected boolean deleteMode = false;

    static
    {
        domMappings = new HashMap<String, String>();
        domMappings.put("domGeometry", "dom-geometry");
        domMappings.put("domConstruct", "dom-construct");
        domMappings.put("domAttr", "dom-attr");
        domMappings.put("domClass", "dom-class");
        domMappings.put("domStyle", "dom-style");
    }

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

        JPanel panel = new JPanel();
        JLabel label = new JLabel();
        label.setText(String.format("Detected %d unused imports in this file", parameters.size()));
        panel.add(label);
        WindowManager.getInstance().getStatusBar(e.getProject()).fireNotificationPopup(panel, Color.green);

        if(this.deleteMode)
        {
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
                        elementsToDelete.add(element.getNextSibling());
                    }

                    for(PsiElement element : elementsToDelete)
                    {
                        try
                        {
                            results.append("deleted " + element.getText() + "\n");
                            element.delete();
                        }
                        catch(Exception e)
                        {

                        }
                    }

                    final ToolWindow window = ToolWindowManager.getInstance(e.getProject()).getToolWindow("Dojo Unused Imports");
                    window.activate(new Runnable() {
                        @Override
                        public void run() {
                            JTextArea textarea = DojoImportToolWindow.getSharedTextArea();
                            textarea.setText(results.toString());
                        }
                    });
                }
            });
        }
    }

    @NotNull
    private Boolean inMap(@NotNull String text, String unused)
    {
        return domMappings.containsKey(unused) && text.contains(domMappings.get(unused));
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
