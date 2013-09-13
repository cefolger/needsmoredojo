package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.objectmodel.AMDValidator;
import com.chrisfolger.needsmoredojo.core.util.HighlightingUtil;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.TemplatedWidgetUtil;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JumpToAttachPointAction extends JavaScriptAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);

        if(editor == null || file == null)
        {
            return;
        }

        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());

        if(element == null)
        {
            return;
        }

        PsiFile templateFile = new TemplatedWidgetUtil(file).findTemplatePath();

        if(templateFile == null)
        {
            Notifications.Bus.notify(new Notification("needsmoredojo", "Jump To Attach Point", "No attach point found for " + element.getText(), NotificationType.INFORMATION));
            return;
        }

        jumpToElementInTemplate(templateFile, element);
    }

    private void jumpToElementInTemplate(PsiFile templateFile, PsiElement sourceElement)
    {
        if(!AMDValidator.elementIsAttachPoint(sourceElement))
        {
            Notifications.Bus.notify(new Notification("needsmoredojo", "Jump To Attach Point", "Element is not an attach point or is in an invalid statement with an attach point: '" + sourceElement.getText() + "'", NotificationType.INFORMATION));
            return;
        }

        FileEditorManager.getInstance(templateFile.getProject()).openFile(templateFile.getVirtualFile(), true, true);
        Editor editor = EditorFactory.getInstance().getEditors(PsiDocumentManager.getInstance(templateFile.getProject()).getDocument(templateFile))[0];
        Document document = PsiDocumentManager.getInstance(templateFile.getProject()).getDocument(templateFile);

        String documentText = document.getText();
        Pattern[] searchPatterns = TemplatedWidgetUtil.getAttachPointStringFromReference(sourceElement);
        int indexOfAttachPoint = -1;

        for(Pattern pattern : searchPatterns)
        {
            indexOfAttachPoint = TemplatedWidgetUtil.indexOf(pattern, documentText);
            if(indexOfAttachPoint > -1)
            {
                break;
            }
        }

        if(indexOfAttachPoint == -1)
        {
            // this is the last resort, when an attach point is just found because it was invalid, jump back to the previous file
            FileEditorManager.getInstance(templateFile.getProject()).openFile(sourceElement.getContainingFile().getVirtualFile(), true, true);
            Notifications.Bus.notify(new Notification("needsmoredojo", "Jump To Attach Point", "Attach point not found in " + templateFile.getVirtualFile().getName() + ": '" + sourceElement.getText() + "'", NotificationType.INFORMATION));
            return;
        }

        editor.getCaretModel().moveToOffset(indexOfAttachPoint);
        PsiElement element = templateFile.findElementAt(indexOfAttachPoint);

        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);

        List<PsiElement> elementsToHighlight = new ArrayList<PsiElement>();
        elementsToHighlight.add(element);
        if(element.getNextSibling() != null)
        {
            elementsToHighlight.add(element.getNextSibling());
            if(element.getNextSibling().getNextSibling() != null)
            {
                elementsToHighlight.add(element.getNextSibling().getNextSibling());
            }
        }

        HighlightingUtil.highlightElement(editor, templateFile.getProject(), elementsToHighlight.toArray(new PsiElement[0]));
    }

    @Override
    public void update(AnActionEvent e)
    {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        if(editor == null)
        {
            e.getPresentation().setEnabled(false);
            return;
        }

        PsiFile file = e.getData(LangDataKeys.PSI_FILE);

        if(file == null)
        {
            e.getPresentation().setEnabled(false);
            return;
        }

        e.getPresentation().setEnabled(true);

        if(!(file.getFileType() instanceof JavaScriptFileType))
        {
            e.getPresentation().setEnabled(false);
            return;
        }

        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        if(!AMDValidator.elementIsAttachPoint(element))
        {
            e.getPresentation().setEnabled(false);
        }
    }
}
