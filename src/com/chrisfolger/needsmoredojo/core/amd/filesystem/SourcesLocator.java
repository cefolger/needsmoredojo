package com.chrisfolger.needsmoredojo.core.amd.filesystem;

import com.chrisfolger.needsmoredojo.core.amd.SourceLibrary;
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
import java.util.Collections;
import java.util.Comparator;
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

    public @Nullable SourceLibrary getFirstLibraryThatIncludesFile(@NotNull String fileCanonicalPath, @NotNull SourceLibrary[] libraries)
    {
        int firstIndex = Integer.MAX_VALUE;
        SourceLibrary firstLibrary = null;

        for(SourceLibrary library : libraries)
        {
            String fileWithoutLibraryPath = fileCanonicalPath;
            if(fileWithoutLibraryPath.indexOf(library.getPath()) != -1)
            {
                fileWithoutLibraryPath = library.getName() + fileWithoutLibraryPath.substring(fileWithoutLibraryPath.indexOf(library.getPath()) + library.getPath().length());
            }

            int index = fileWithoutLibraryPath.indexOf(library.getName());
            if(index > -1 && index < firstIndex)
            {
                firstIndex = index;
                firstLibrary = library;
            }
        }

        return firstLibrary;
    }

    /**
     * Gets a list of dojo packages in the project
     * @param project
     * @return
     */
    public List<SourceLibrary> getSourceLibraries(Project project)
    {
        List<SourceLibrary> libraries = new ArrayList<SourceLibrary>();

        try
        {
            VirtualFile dojoSourcesParentDirectory = SourcesLocator.getDojoSourcesDirectory(project, true);
            if(dojoSourcesParentDirectory != null)
            {
                for(VirtualFile directory : dojoSourcesParentDirectory.getChildren())
                {
                    SourceLibrary library = new SourceLibrary(directory.getName(), directory.getCanonicalPath(), true);
                    libraries.add(library);
                }
            }

            VirtualFile[] otherSourceDirectories = SourcesLocator.getProjectSourceDirectories(project, true);
            for(VirtualFile directory : otherSourceDirectories)
            {
                for(VirtualFile sourceDirectory : directory.getChildren())
                {
                    if(sourceDirectory.getName().contains("."))
                    {
                        continue; // file or hidden directory
                    }

                    SourceLibrary library = new SourceLibrary(sourceDirectory.getName(), sourceDirectory.getCanonicalPath(), true);
                    libraries.add(library);
                }
            }
        }
        catch(NullPointerException exc)
        {
            return libraries;
        }

        Collections.sort(libraries, new Comparator<SourceLibrary>() {
            @Override
            public int compare(SourceLibrary o1, SourceLibrary o2) {
                return o2.getName().length() - o1.getName().length();
            }
        });

        return libraries;
    }
}
