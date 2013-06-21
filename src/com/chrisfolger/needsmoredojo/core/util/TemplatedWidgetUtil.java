package com.chrisfolger.needsmoredojo.core.util;

import com.chrisfolger.needsmoredojo.core.amd.DeclareFinder;
import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.intellij.find.actions.FindUsagesInFileAction;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.PsiSearchScopeUtil;
import com.intellij.psi.search.SearchScope;

import java.util.ArrayList;

public class TemplatedWidgetUtil implements DeclareFinder.CompletionCallback {
    private DeclareFinder.CompletionCallback onTemplatePathFound;
    private PsiFile file;

    public TemplatedWidgetUtil(PsiFile file, DeclareFinder.CompletionCallback onTemplatePathFound)
    {
        this.onTemplatePathFound = onTemplatePathFound;
        this.file = file;
    }

    public String findTemplateFromDeclare(DeclareUtil.DeclareStatementItems statement)
    {
        for(JSProperty property : statement.getMethodsToConvert())
        {
            if(property.getName().equals("templateString"))
            {
                String template = property.getValue().getText();

                ArrayList<PsiElement> defines = new ArrayList<PsiElement>();
                ArrayList<PsiElement> parameters = new ArrayList<PsiElement>();

                new DefineResolver().gatherDefineAndParameters(file, defines, parameters);

                // find the parameter and define that matches the template parameter
                PsiElement relevantDefine = null;
                for(int i = 0;i<parameters.size();i++)
                {
                    if(parameters.get(i).getText().equals(template))
                    {
                        // TODO support absolute paths (for now only support relative)
                        relevantDefine = defines.get(i);
                    }
                }

                String templatePath = relevantDefine.getText().substring(relevantDefine.getText().lastIndexOf('!') + 1);
                String parsedPath = templatePath.replaceFirst("./", "/").replaceAll("'", "").replaceAll("\"", "");
                // now open the file and find the reference in it
                VirtualFile htmlFile = file.getContainingDirectory().getVirtualFile().findFileByRelativePath(parsedPath);

                PsiFile templateFile = PsiManager.getInstance(file.getProject()).findFile(htmlFile);
                FileEditor editor = FileEditorManager.getInstance(file.getProject()).openFile(htmlFile, true, true)[0];
                

                int i=0;
            }
        }

        return "";
    }

    public void findTemplatePath()
    {
        file.acceptChildren(new DeclareFinder().getDefineVisitorToRetrieveDeclareObject(this));
    }

    @Override
    public void run(Object[] result)
    {
        final DeclareUtil.DeclareStatementItems utilItem = new DeclareUtil().getDeclareStatementFromParsedStatement(result);

        this.onTemplatePathFound.run(new Object[] {
            findTemplateFromDeclare(utilItem)
        });
    }
}
