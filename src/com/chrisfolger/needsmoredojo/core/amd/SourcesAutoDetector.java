package com.chrisfolger.needsmoredojo.core.amd;

import com.intellij.lang.StdLanguages;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSRecursiveElementVisitor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SourcesAutoDetector
{
    private boolean isDojoModule(String path)
    {
        return path.startsWith("dojo") || path.startsWith("dijit") || path.startsWith("dgrid") || path.startsWith("dojox");
    }

    private boolean isInDojoSources(String directory)
    {
        return directory.contains("/dojo/") || directory.contains("/dijit/") || directory.contains("/dojox/") || directory.contains("/dgrid/") || directory.contains("/util/buildscripts/");
    }

    private Set<String> getDojoModulesInHtmlFile(PsiFile file)
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

                super.visitXmlAttribute(attribute);    //To change body of overridden methods use File | Settings | File Templates.
            }
        });

        return modules;
    }

    public PsiFile[] getPossibleSourceRoots(Project project)
    {
        /**
         * here's how we guess where the project root is:
         * open index.html to search for amd module references
         * open javascript files, check if they are amd modules, and if so find the root of them
         */

        PsiFile[] files = FilenameIndex.getFilesByName(project, "index.html", GlobalSearchScope.projectScope(project));
        List<PsiFile> potentialFiles = new ArrayList<PsiFile>();

        for(PsiFile file : files)
        {
            String directory = file.getContainingDirectory().getVirtualFile().getCanonicalPath();
            if(!isInDojoSources(directory))
            {
                potentialFiles.add(file);
            }
        }

        for(PsiFile file : potentialFiles)
        {
            getDojoModulesInHtmlFile(file);
        }

        return files;
    }
}
