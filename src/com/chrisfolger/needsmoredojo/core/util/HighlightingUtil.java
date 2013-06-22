package com.chrisfolger.needsmoredojo.core.util;

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class HighlightingUtil
{
    public static void highlightElement(@NotNull com.intellij.openapi.project.Project project, @NotNull PsiElement[] elements)
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
                        EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES);

        highlightManager.addOccurrenceHighlights(
                editor, elements, textattributes, true, null);
        final WindowManager windowManager = WindowManager.getInstance();
        final StatusBar statusBar = windowManager.getStatusBar(project);
        statusBar.setInfo("Press Esc to remove highlighting");
    }
}
