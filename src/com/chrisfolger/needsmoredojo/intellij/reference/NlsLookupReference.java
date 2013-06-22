package com.chrisfolger.needsmoredojo.intellij.reference;

import com.chrisfolger.needsmoredojo.core.amd.DefineResolver;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NlsLookupReference extends PsiReferenceBase<JSLiteralExpression> {
    private PsiElement qualifier;
    private JSIndexedPropertyAccessExpression accessor;

    public NlsLookupReference(PsiElement qualifier, JSIndexedPropertyAccessExpression accessor, JSLiteralExpression sourceElement)
    {
        super(sourceElement);

        this.qualifier = qualifier;
        this.accessor = accessor;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        // get the list of defines
        // find one that matches
        // check to see if it's an i18n file
        // resolve the reference to the file
        List<PsiElement> defines = new ArrayList<PsiElement>();
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        new DefineResolver().gatherDefineAndParameters(qualifier.getContainingFile(), defines, parameters);

        PsiElement correctDefine = null;
        for(int i=0;i<parameters.size();i++)
        {
            if(parameters.get(i).getText().equals(qualifier.getText()))
            {
                correctDefine = defines.get(i);
            }
        }

        // didn't get a define, so there is no reference to an i18n item
        if(correctDefine == null)
        {
            return null;
        }

        String defineText = correctDefine.getText();
        defineText = defineText.substring(defineText.lastIndexOf("!") + 1).replaceAll("'", "");

        // TODO find relative path etc.
        PsiFile[] files = FilenameIndex.getFilesByName(correctDefine.getProject(), "dojo.js", GlobalSearchScope.projectScope(correctDefine.getProject()));
        PsiFile dojoFile = null;

        for(PsiFile file : files)
        {
            if(file.getContainingDirectory().getName().equals("dojo"))
            {
                dojoFile = file;
                break;
            }
        }

        VirtualFile i18nFile = dojoFile.getContainingDirectory().getParent().getVirtualFile().findFileByRelativePath("/" + defineText + ".js");
        PsiFile templateFile = PsiManager.getInstance(dojoFile.getProject()).findFile(i18nFile);

        final PsiElement[] i18nElement = {null};
        templateFile.acceptChildren(new JSRecursiveElementVisitor() {
            @Override
            public void visitJSObjectLiteralExpression(JSObjectLiteralExpression node)
            {
                if(!node.getParent().getText().startsWith("root:"))
                {
                    super.visitJSObjectLiteralExpression(node);
                    return;
                }

                for(JSProperty property : node.getProperties())
                {
                    String propertyText = accessor.getIndexExpression().getText();
                    propertyText = propertyText.substring(1, propertyText.length() - 1);

                    if(property.getName().equals(propertyText))
                    {
                        i18nElement[0] = property;
                    }
                }

                super.visitJSObjectLiteralExpression(node);
            }
        });

        return i18nElement[0];
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[] { "foo"} ;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
