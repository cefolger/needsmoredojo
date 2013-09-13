package com.chrisfolger.needsmoredojo.core.amd.objectmodel;

import com.chrisfolger.needsmoredojo.core.amd.CompletionCallback;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.lang.javascript.psi.JSBinaryExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplatedWidgetUtil {
    private PsiFile file;

    public TemplatedWidgetUtil(PsiFile file)
    {
        this.file = file;
    }

    public PsiFile findTemplatePath()
    {
        final DeclareStatementItems[] utilItem = new DeclareStatementItems[1];

        // this will call run() when the declare object is found
        file.acceptChildren(new DeclareResolver().getDefineVisitorToRetrieveDeclareObject(new CompletionCallback() {
            @Override
            public void run(Object[] result) {
                utilItem[0] = new DeclareResolver().getDeclareStatementFromParsedStatement(result);
            }
        }));

        return findTemplateFromDeclare(utilItem[0]);
    }

    public PsiFile findTemplateFromDeclare(@Nullable DeclareStatementItems statement)
    {
        if(statement == null)
        {
            return null;
        }

        for(JSProperty property : statement.getMethodsToConvert())
        {
            // just continue if this property is invalid for some reason
            if(property == null || property.getName() == null || property.getValue() == null)
            {
                continue;
            }

            /**
             * have to account for these scenarios
             * templateString: <reference to an imported template>
             * templateString: 'inline template'
             * templateString: 'inline template ' +
             *                  ' spanning multiple lines '
             */
            if(property.getName().equals("templateString"))
            {
                String template = property.getValue().getText();

                if(property.getValue() instanceof JSLiteralExpression || property.getValue() instanceof JSBinaryExpression)
                {
                    return property.getContainingFile();
                }
                else
                {
                    // find the parameter and define that matches the template parameter
                    PsiElement relevantDefine = AMDPsiUtil.getDefineForVariable(file, template);

                    String templatePath = relevantDefine.getText().substring(relevantDefine.getText().lastIndexOf('!') + 1);
                    // now open the file and find the reference in it
                    VirtualFile htmlFile = SourcesLocator.getAMDImportFile(relevantDefine.getProject(), templatePath, relevantDefine.getContainingFile().getContainingDirectory());

                    PsiFile templateFile = PsiManager.getInstance(file.getProject()).findFile(htmlFile);
                    return templateFile;
                }
            }
        }

        return null;
    }

    /** @return index of pattern in s or -1, if not found */
    public static int indexOf(Pattern pattern, String s) {
        Matcher matcher = pattern.matcher(s);
        return matcher.find() ? matcher.start() : -1;
    }

    public static Pattern[] getAttachPointStringFromReference(PsiElement reference)
    {
        // have to use a regex in case there are multiple attach points specified
        // there is also a legacy attribute format that we want to support because
        // alot of the dojo library
        return new Pattern[] {
                Pattern.compile("data-dojo-attach-point=\\\"(\\w|,)*" + reference.getText() + "\\w*\\\""),
                Pattern.compile("dojoAttachPoint=\\\"(\\w|,)*" + reference.getText() + "\\w*\\\"")
        };
    }
}
