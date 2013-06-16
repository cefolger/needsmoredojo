package com.chrisfolger.needsmoredojo.base;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.psi.PsiElement;

public class JSUtil
{
    public static void addStatementBeforeElement(PsiElement parent, PsiElement element, String statement)
    {
        ASTNode node = JSChangeUtil.createStatementFromText(parent.getProject(), statement, JSUtils.getDialect(parent.getContainingFile()));
        parent.addBefore(node.getPsi(), element);
        parent.addBefore(JSChangeUtil.createJSTreeFromText(parent.getProject(), "\n\n").getPsi(), element);
    }
}
