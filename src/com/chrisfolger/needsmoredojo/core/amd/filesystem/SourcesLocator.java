package com.chrisfolger.needsmoredojo.core.amd.filesystem;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SourcesLocator
{
    public static @Nullable VirtualFile getAMDImportFile(Project project, String modulePath, PsiDirectory sourceFileParentDirectory)
    {
        for(VirtualFile source : getAllSourceDirectories(project, true))
        {
            VirtualFile result = null;

            String parsedPath = modulePath.replaceAll("('|\")", "");
            if(parsedPath.charAt(0) != '.') // this means it's not a relative path, but rather a defined package path
            {
                parsedPath = "/" + parsedPath;
                if(source == null)
                {
                    return null;
                }

                result = source.findFileByRelativePath(parsedPath);
            }
            else
            {
                result = sourceFileParentDirectory.getVirtualFile().findFileByRelativePath(parsedPath);
            }

            if(result != null)
            {
                return result;
            }
        }

        return null;
    }

    public static @NotNull VirtualFile[] getProjectSourceDirectories(Project project, boolean pullFromSettings)
    {
        DojoSettings settingsService = ServiceManager.getService(project, DojoSettings.class);
        String projectLibrary = settingsService.getProjectSourcesDirectory();

        // it's an array in case I decide to add multiple non-dojo source library capability
        if(projectLibrary != null && !projectLibrary.equals("") && pullFromSettings)
        {
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(projectLibrary);
            return new VirtualFile[] {  file };
        }
        else
        {
            // return the dojo library sources
            return new VirtualFile[] { getDojoSourcesDirectory(project, pullFromSettings) };
        }
    }

    public static @NotNull VirtualFile[] getAllSourceDirectories(Project project, boolean pullFromSettings)
    {
        List<VirtualFile> sourceFiles = new ArrayList<VirtualFile>();
        for(VirtualFile file : getProjectSourceDirectories(project, pullFromSettings))
        {
            sourceFiles.add(file);
        }
        sourceFiles.add(getDojoSourcesDirectory(project, true));

        return sourceFiles.toArray(new VirtualFile[0]);
    }

    public static @Nullable VirtualFile getDojoSourcesDirectory(Project project, boolean pullFromSettings)
    {
        DojoSettings settingsService = ServiceManager.getService(project, DojoSettings.class);
        String dojoLibrary = settingsService.getDojoSourcesDirectory();
        String projectSources = settingsService.getProjectSourcesDirectory();

        if(projectSources != null && pullFromSettings && settingsService.isDojoSourcesShareProjectSourcesRoot())
        {
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(projectSources);
            return file;
        }
        else if(dojoLibrary != null && !dojoLibrary.equals("") && pullFromSettings)
        {
            VirtualFile file = null;

            // this means the dojo sources are in an archive
            if(dojoLibrary.contains("!"))
            {
                file = JarFileSystem.getInstance().findFileByPath(dojoLibrary);
            }
            else
            {
                file = LocalFileSystem.getInstance().findFileByPath(dojoLibrary);
            }

            return file;
        }
        else
        {
            PsiFile[] files = FilenameIndex.getFilesByName(project, "dojo.js", GlobalSearchScope.projectScope(project));
            PsiFile dojoFile = null;

            for(PsiFile file : files)
            {
                if(file.getContainingDirectory().getName().equals("dojo"))
                {
                    dojoFile = file;
                    break;
                }
            }

            if(dojoFile != null)
            {
                return dojoFile.getContainingDirectory().getParent().getVirtualFile();
            }
        }

        return null;
    }
}
