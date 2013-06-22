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
    private PsiFile file;

    public NlsLookupReference(PsiElement qualifier, JSIndexedPropertyAccessExpression accessor, JSLiteralExpression sourceElement)
    {
        super(sourceElement);

        this.qualifier = qualifier;
        this.accessor = accessor;
    }

    public List<JSProperty> getI18nKeys(PsiFile file)
    {
        final List<JSProperty> keys = new ArrayList<JSProperty>();
        file.acceptChildren(new JSRecursiveElementVisitor() {
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
                    keys.add(property);
                }

                super.visitJSObjectLiteralExpression(node);
            }
        });

        return keys;
    }

    public PsiFile getFileContainingI18nKeys()
    {
        if(file != null)
        {
            return file;
        }

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

        file = templateFile;
        return templateFile;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        PsiFile templateFile = getFileContainingI18nKeys();

        if(templateFile == null)
        {
            return null;
        }

        for(JSProperty property : getI18nKeys(templateFile))
        {
            String propertyText = accessor.getIndexExpression().getText();
            propertyText = propertyText.substring(1, propertyText.length() - 1);

            if(property.getName().equals(propertyText))
            {
                return property;
            }
        }

        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        PsiFile templateFile = getFileContainingI18nKeys();
        if(templateFile == null)
        {
            return new Object[0];
        }

        List<JSProperty> keys = getI18nKeys(file);
        List<Object> keyStrings = new ArrayList<Object>();

        for(JSProperty key : keys)
        {
            keyStrings.add(key.getName());
        }

        return keyStrings.toArray();
    }
}
