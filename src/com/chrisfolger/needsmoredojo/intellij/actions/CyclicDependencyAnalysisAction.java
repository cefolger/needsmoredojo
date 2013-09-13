package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.define.AMDImportOrganizer;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.util.PsiFileUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * this is an action that takes a bunch of AMD imports and organizes them alphabetically
 */
public class CyclicDependencyAnalysisAction extends JavaScriptAction
{
    private Set<String> dependencies;

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        dependencies = new HashSet<String>();

        final PsiFile psiFile = PsiFileUtil.getPsiFileInCurrentEditor(e.getProject());

        addDependenciesOfFile(e.getProject(), psiFile, dependencies);

        if(dependencies.contains(psiFile.getName()))
        {
            int i=0;
        }
        int i=0;
    }

    private void addDependenciesOfFile(Project project, PsiFile psiFile, Set<String> dependencies)
    {
        DefineResolver resolver = new DefineResolver();
        final List<PsiElement> parameters = new ArrayList<PsiElement>();
        final List<PsiElement> defines = new ArrayList<PsiElement>();

        resolver.gatherDefineAndParameters(psiFile, defines, parameters);

        for(PsiElement element : defines)
        {
            String define = element.getText().replaceAll("'", "").replaceAll("\"", "") + ".js";

            // TODO the correct way to do this is probably through ModuleImporter
            // now open the file and find the reference in it
            VirtualFile htmlFile = SourcesLocator.getAMDImportFile(element.getProject(), define, psiFile.getContainingFile().getContainingDirectory());

            if(DojoModuleFileResolver.isDojoModule(element.getText().replaceAll("'", "").replaceAll("\"", "")))
            {
                continue;
            }

            if(htmlFile == null)
            {
                continue;
            }

            PsiFile templateFile = PsiManager.getInstance(psiFile.getProject()).findFile(htmlFile);

            if(dependencies.contains(templateFile.getName()))
            {
                continue;
            }

            dependencies.add(templateFile.getName());
            addDependenciesOfFile(project, templateFile, dependencies);
        }
    }

}

