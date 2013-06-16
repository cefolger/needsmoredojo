package com.chrisfolger.needsmoredojo.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Chris
 * Date: 12/20/12
 * Time: 7:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class DojoImportToolWindowFactory implements ToolWindowFactory
{
    private ToolWindow myToolWindow;
    private JPanel myToolWindowContent;

    @Override
    public void createToolWindowContent(Project project, @NotNull ToolWindow toolWindow)
    {
        DojoImportToolWindow result = new DojoImportToolWindow();
        myToolWindowContent = result.getPanel();
        DojoImportToolWindow.setSharedTextArea(result.getTextArea());

                // Create the tool window content.
        myToolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
