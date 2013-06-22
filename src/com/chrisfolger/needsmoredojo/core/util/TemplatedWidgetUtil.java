package com.chrisfolger.needsmoredojo.core.util;

import com.chrisfolger.needsmoredojo.core.amd.DeclareFinder;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

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
        /*
            It's hard to detect when an element is an attach point, because of the use of this inside other functions

            this.attachpoint
            that.attachpoint

            ideally we would parse the template file in the beginning and cache all of the attach points,
            maybe that's a todo item...
         */
        if(element.getParent() == null || !(element.getParent() instanceof JSReferenceExpression))
        {
            return false;
        }

        // we can exclude JSCallExpressions at least because you will never reference an attach point like
        // this.attachpoint(...)
        if(element.getParent().getParent() instanceof JSCallExpression)
        {
            return false;
        }

        return true;
    }

    public static String getAttachPointStringFromReference(PsiElement reference)
    {
        return "data-dojo-attach-point=\"" + reference.getText() + "\"";
    }
}
