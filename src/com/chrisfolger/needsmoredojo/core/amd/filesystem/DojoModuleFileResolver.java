package com.chrisfolger.needsmoredojo.core.amd.filesystem;

import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSRecursiveElementVisitor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DojoModuleFileResolver
{
    public static boolean isDojoModule(String path)
    {
        return path.startsWith("dojo") || path.startsWith("dijit") || path.startsWith("dgrid") || path.startsWith("dojox");
    }

    public static boolean isInDojoSources(String directory)
    {
        return directory.contains("/xstyle/") || directory.contains("/nls/") || directory.contains("/dojo/") || directory.contains("/dijit/") || directory.contains("/dojox/") || directory.contains("/dgrid/") || directory.contains("/util/buildscripts/");
    }

    public Set<String> getDojoModulesInJavaScriptFiles(Project project)
    {
        Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(project, "js");
        Set<PsiFile> potentialFiles = new HashSet<PsiFile>();

        for(VirtualFile file : files)
        {
            if(isInDojoSources(file.getCanonicalPath())) continue;

            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            potentialFiles.add(psiFile);
        }

        Set<String> matches = new HashSet<String>();
        HashSet<PsiDirectory> attemptedDirectories = new HashSet<PsiDirectory>();
        for(PsiFile file : potentialFiles)
        {
            if(attemptedDirectories.contains(file.getParent())) continue;

            attemptedDirectories.add(file.getParent());
            if(file.getText().contains("define("))
            {
                String path = file.getVirtualFile().getCanonicalPath();
                path = path.substring(path.lastIndexOf('/') + 1);
                path = path.substring(0, path.length() - 3);
                matches.add(path);
            }
        }

        return matches;
    }

    public Set<String> getDojoModulesInHtmlFile(PsiFile file)
    {
        final Set<String> modules = new HashSet<String>();

        file.acceptChildren(new JSRecursiveElementVisitor() {
            @Override
            public void visitJSCallExpression(JSCallExpression node) {
                if(!node.getText().startsWith("require"))
                {
                    super.visitJSCallExpression(node);
                    return;
                }

                if(node.getArguments().length > 0 && node.getArguments()[0] instanceof JSArrayLiteralExpression)
                {
                    JSArrayLiteralExpression arguments = (JSArrayLiteralExpression) node.getArguments()[0];
                    for(JSExpression literal : arguments.getExpressions())
                    {
                        String literalText = literal.getText().replaceAll("'", "").replaceAll("\"", "");

                        if(!isDojoModule(literalText))
                        {
                            modules.add(literalText);
                        }
                    }
                }

                super.visitJSCallExpression(node);
            }
        });

        file.acceptChildren(new XmlRecursiveElementVisitor() {
            @Override
            public void visitXmlTag(XmlTag tag) {
                super.visitXmlTag(tag);
            }

            @Override
            public void visitXmlAttribute(XmlAttribute attribute) {
                if(attribute.getName().equals("data-dojo-type"))
                {
                    if(!isDojoModule(attribute.getValue()))
                    {
                        modules.add(attribute.getValue());
                    }
                }

                super.visitXmlAttribute(attribute);
            }
        });

        return modules;
    }

    public Set<String> getDirectoriesForDojoModules(Project project, Set<String> modules)
    {
        Set<String> possibleDirectories = new HashSet<String>();

        for(String module : modules)
        {
            String moduleParent = module;

            if(module.contains("/"))
            {
                module = module.substring(module.lastIndexOf("/") + 1);
            }

            PsiFile[] files = FilenameIndex.getFilesByName(project, module + ".js", GlobalSearchScope.projectScope(project));

            for(PsiFile file : files)
            {
                if( file.getVirtualFile().getCanonicalPath().contains(moduleParent))
                {
                    possibleDirectories.add(file.getParent().getParent().getName() +  " (" + file.getParent().getParent().getVirtualFile().getCanonicalPath() + ")");
                }
            }
        }

        return possibleDirectories;
    }
}
