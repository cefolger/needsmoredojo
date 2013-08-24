package com.chrisfolger.needsmoredojo.intellij.components;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;

public class DojoSettingsDetectionComponent implements ProjectComponent {
    private Project project;

    private class SetupNotification implements NotificationListener {
        @Override
        public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent hyperlinkEvent) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, "Needs More Dojo");
        }
    }

    public DojoSettingsDetectionComponent(Project project) {
        this.project = project;
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "DojoSettingsDetectionComponent";
    }

    private boolean needsSetup()
    {
        DojoSettings settingsService = ServiceManager.getService(project, DojoSettings.class);
        if( (settingsService.getDojoSourcesDirectory() == null || settingsService.getDojoSourcesDirectory().equals("")) && !settingsService.isDojoSourcesShareProjectSourcesRoot())
        {
            return true;
        }

        if(settingsService.getProjectSourcesDirectory() == null || settingsService.getProjectSourcesDirectory().equals(""))
        {
            return true;
        }

        return false;
    }

    public void projectOpened() {
        if(!needsSetup())
        {
            return;
        }

        // called when project is opened
        new Notification("needsmoredojo", "Needs More Dojo: Setup Sources", "It looks like you haven't set up dojo or project sources, which might make some features of Needs More Dojo work incorrectly. <a href=\"setup\">Set them up now...</a>", NotificationType.WARNING, new SetupNotification()).notify(project);
    }

    public void projectClosed() {
    }
}
