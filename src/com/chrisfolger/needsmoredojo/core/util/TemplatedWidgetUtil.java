package com.chrisfolger.needsmoredojo.core.util;

import com.chrisfolger.needsmoredojo.core.amd.DeclareFinder;
import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.util.ArrayList;

public class TemplatedWidgetUtil {
    private PsiFile file;

    public TemplatedWidgetUtil(PsiFile file)
    {
        this.file = file;
    }

    public PsiFile findTemplatePath()
    {
        final DeclareUtil.DeclareStatementItems[] utilItem = new DeclareUtil.DeclareStatementItems[1];

        // this will call run() when the declare object is found
        file.acceptChildren(new DeclareFinder().getDefineVisitorToRetrieveDeclareObject(new DeclareFinder.CompletionCallback() {
            @Override
            public void run(Object[] result) {
                utilItem[0] = new DeclareUtil().getDeclareStatementFromParsedStatement(result);
            }
        }));

        return findTemplateFromDeclare(utilItem[0]);
    }

    public PsiFile findTemplateFromDeclare(DeclareUtil.DeclareStatementItems statement)
    {
        for(JSProperty property : statement.getMethodsToConvert())
        {
            if(property.getName().equals("templateString"))
            {
                String template = property.getValue().getText();

                // find the parameter and define that matches the template parameter
                PsiElement relevantDefine = AMDUtil.getDefineForVariable(file, template);

                String templatePath = relevantDefine.getText().substring(relevantDefine.getText().lastIndexOf('!') + 1);
                // now open the file and find the reference in it
                VirtualFile htmlFile = AMDUtil.getAMDImportFile(relevantDefine.getProject(), templatePath, relevantDefine.getContainingFile().getContainingDirectory());

                PsiFile templateFile = PsiManager.getInstance(file.getProject()).findFile(htmlFile);
                return templateFile;
            }
        }

        return null;
    }

    public static boolean elementIsAttachPoint(PsiElement element)
    {
        // conditions for an element to be an attach point:
        // must be inside of a this expression
        // must have no references?

        return true;
    }

    public static String getAttachPointStringFromReference(PsiElement reference)
    {
        return "data-dojo-attach-point=\"" + reference.getText() + "\"";
    }
}
