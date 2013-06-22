package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.util.HighlightingUtil;
import com.chrisfolger.needsmoredojo.core.util.TemplatedWidgetUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
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

public class JumpToAttachPointAction extends AnAction
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
        if(!TemplatedWidgetUtil.elementIsAttachPoint(sourceElement))
        {
            Notifications.Bus.notify(new Notification("needsmoredojo", "Jump To Attach Point", "Element is not an attach point or is in an invalid statement with an attach point: '" + sourceElement.getText() + "'", NotificationType.INFORMATION));
            return;
        }

        FileEditorManager.getInstance(templateFile.getProject()).openFile(templateFile.getVirtualFile(), true, true);
        Editor editor = EditorFactory.getInstance().getEditors(PsiDocumentManager.getInstance(templateFile.getProject()).getDocument(templateFile))[0];
        Document document = PsiDocumentManager.getInstance(templateFile.getProject()).getDocument(templateFile);

        String documentText = document.getText();
        int indexOfAttachPoint = documentText.indexOf(TemplatedWidgetUtil.getAttachPointStringFromReference(sourceElement));
        if(indexOfAttachPoint == -1)
        {
            // this is the last resort, when an attach point is just found because it was invalid, jump back to the previous file
            FileEditorManager.getInstance(templateFile.getProject()).openFile(sourceElement.getContainingFile().getVirtualFile(), true, true);
            Notifications.Bus.notify(new Notification("needsmoredojo", "Jump To Attach Point", "Attach point not found in file: '" + sourceElement.getText() + "'", NotificationType.INFORMATION));
            return;
        }

        editor.getCaretModel().moveToOffset(indexOfAttachPoint);
        PsiElement element = templateFile.findElementAt(indexOfAttachPoint);

        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
        HighlightingUtil.highlightElement(templateFile.getProject(), new PsiElement[]{element, element.getNextSibling(), element.getNextSibling().getNextSibling()});
    }

    @Override
    public void update(AnActionEvent e)
    {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);

        e.getPresentation().setEnabled(true);

        if(editor == null || file == null)
        {
            e.getPresentation().setEnabled(false);
        }
    }
}
