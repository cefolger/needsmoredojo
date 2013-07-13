package com.chrisfolger.needsmoredojo.core.util;

import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AMDUtil
{
    public static final String I18NPLUGIN = "dojo/i18n!";
    public static final String TEXTPLUGIN = "dojo/text!";

    public static PsiElement getDefineForVariable(PsiFile file, String textToCompare)
    {
        List<PsiElement> defines = new ArrayList<PsiElement>();
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        new DefineResolver().gatherDefineAndParameters(file, defines, parameters);

        for(int i=0;i<parameters.size();i++)
        {
            if(i > defines.size() - 1)
            {
                return null; // amd import is being modified
            }

            if(parameters.get(i).getText().equals(textToCompare))
            {
                return defines.get(i);
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

        if(dojoLibrary != null && !dojoLibrary.equals("") && pullFromSettings)
        {
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(dojoLibrary);
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

    public static String defineToParameter(String define, Map<String, String> exceptions)
    {
        // since there are two fx modules we have this exception
        if(define.contains("/_base/fx"))
        {
            return "baseFx";
        }

        // check all exceptions
        if(exceptions.containsKey(define))
        {
            return exceptions.get(define);
        }

        if(define.startsWith(TEXTPLUGIN) || define.startsWith(I18NPLUGIN))
        {
            String postPlugin = define.substring(define.indexOf('!') + 1);

            if(postPlugin.indexOf('/') != -1)
            {
                postPlugin = postPlugin.substring(postPlugin.lastIndexOf('/') + 1);
            }

            if(postPlugin.indexOf('.') != -1)
            {
                postPlugin = postPlugin.substring(0, postPlugin.indexOf('.'));
            }

            if(!define.startsWith(I18NPLUGIN))
            {
                postPlugin = postPlugin.toLowerCase() + "Template";
            }

            return postPlugin;
        }

        String result = define.substring(define.lastIndexOf("/") + 1);
        if(result.contains("-"))
        {
            int index = result.indexOf('-');
            result = result.replace("-", "");
            result = result.substring(0,index)+ ("" +result.charAt(index)).toUpperCase() +result.substring(index+1);
        }

        result = result.replaceAll("_", "");

        return result;
    }
}
