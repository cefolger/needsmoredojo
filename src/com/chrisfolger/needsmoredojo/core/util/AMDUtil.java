package com.chrisfolger.needsmoredojo.core.util;

import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.ArrayList;
import java.util.List;

public class AMDUtil
{
    public static PsiElement getDefineForVariable(PsiFile file, String textToCompare)
    {
        List<PsiElement> defines = new ArrayList<PsiElement>();
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        new DefineResolver().gatherDefineAndParameters(file, defines, parameters);

        for(int i=0;i<parameters.size();i++)
        {
            if(parameters.get(i).getText().equals(textToCompare))
            {
                return defines.get(i);
            }
        }

        return null;
    }

    public static VirtualFile getAMDImportFile(Project project, String modulePath, PsiDirectory sourceFileParentDirectory)
    {
        // TODO other source roots
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

        String parsedPath = modulePath.replaceAll("('|\")", "");
        if(parsedPath.charAt(0) != '.') // this means it's not a relative path, but rather a defined package path
        {
            parsedPath = "/" + parsedPath;
            return dojoFile.getContainingDirectory().getParent().getVirtualFile().findFileByRelativePath(parsedPath);
        }
        else
        {
            return sourceFileParentDirectory.getVirtualFile().findFileByRelativePath(parsedPath);
        }
    }

    public static String defineToParameter(String define)
    {
        // since there are two fx modules we have this exception
        if(define.contains("/_base/fx"))
        {
            return "baseFx";
        }

        if(define.startsWith("dojo/text") || define.startsWith("dojo/i18n"))
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

            if(!define.startsWith("dojo/i18n"))
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
