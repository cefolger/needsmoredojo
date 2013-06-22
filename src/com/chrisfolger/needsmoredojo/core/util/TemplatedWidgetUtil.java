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

public class TemplatedWidgetUtil implements DeclareFinder.CompletionCallback {
    private DeclareFinder.CompletionCallback onTemplatePathFound;
    private PsiFile file;
    private PsiElement sourceElement;

    public TemplatedWidgetUtil(PsiElement sourceElement, PsiFile file, DeclareFinder.CompletionCallback onTemplatePathFound)
    {
        this.onTemplatePathFound = onTemplatePathFound;
        this.file = file;
        this.sourceElement = sourceElement;
    }

    public void findTemplatePath()
    {
        // this will call run() when the declare object is found
        file.acceptChildren(new DeclareFinder().getDefineVisitorToRetrieveDeclareObject(this));
    }

    @Override
    public void run(Object[] result)
    {
        final DeclareUtil.DeclareStatementItems utilItem = new DeclareUtil().getDeclareStatementFromParsedStatement(result);

        // this will call findTemplateFromDeclare when it finds the template that was referenced by the attach point
        this.onTemplatePathFound.run(new Object[] {
                findTemplateFromDeclare(utilItem)
        });
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
                PsiElement relevantDefine = AMDUtil.getDefineForVariable(file, template);

                String templatePath = relevantDefine.getText().substring(relevantDefine.getText().lastIndexOf('!') + 1);
                String parsedPath = templatePath.replaceFirst("./", "/").replaceAll("'", "").replaceAll("\"", "");
                // now open the file and find the reference in it
                VirtualFile htmlFile = AMDUtil.getAMDImportFile(relevantDefine.getProject(), templatePath, relevantDefine.getContainingFile().getContainingDirectory());

                PsiFile templateFile = PsiManager.getInstance(file.getProject()).findFile(htmlFile);
                FileEditor fileEditor = FileEditorManager.getInstance(file.getProject()).openFile(htmlFile, true, true)[0];
                Editor editor = EditorFactory.getInstance().getEditors(PsiDocumentManager.getInstance(templateFile.getProject()).getDocument(templateFile))[0];
                Document document = PsiDocumentManager.getInstance(templateFile.getProject()).getDocument(templateFile);
                String documentText = document.getText();
                editor.getCaretModel().moveToOffset(documentText.indexOf("data-dojo-attach-point=\"" + sourceElement.getText() + "\""));
                PsiElement element = templateFile.findElementAt(documentText.indexOf("data-dojo-attach-point=\"" + sourceElement.getText() + "\""));

                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                HighlightingUtil.highlightElement(templateFile.getProject(), new PsiElement[]{element, element.getNextSibling(), element.getNextSibling().getNextSibling()});
                int i=0;
            }
        }

        return "";
    }
}
