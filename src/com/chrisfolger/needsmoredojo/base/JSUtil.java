package com.chrisfolger.needsmoredojo.base;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.psi.PsiElement;

public class JSUtil
{
    public static PsiElement addStatementBeforeElement(PsiElement parent, PsiElement element, String statement)
    {
        ASTNode node = JSChangeUtil.createStatementFromText(parent.getProject(), statement, JSUtils.getDialect(parent.getContainingFile()));
        parent.addBefore(node.getPsi(), element);
        parent.addBefore(JSChangeUtil.createJSTreeFromText(parent.getProject(), "\n\n").getPsi(), element);

        return node.getPsi();
    }

    public static PsiElement createStatement(PsiElement parent, String statement)
    {
        ASTNode node = JSChangeUtil.createStatementFromText(parent.getProject(), statement, JSUtils.getDialect(parent.getContainingFile()));
        return node.getPsi();
    }

    public static PsiElement createExpression(PsiElement parent, String statement)
    {
        ASTNode node = JSChangeUtil.createExpressionFromText(parent.getProject(), statement, JSUtils.getDialect(parent.getContainingFile()));
        return node.getPsi();
    }

    public static PsiElement addStatement(PsiElement parent, String statement)
    {
        ASTNode node = JSChangeUtil.createStatementFromText(parent.getProject(), statement, JSUtils.getDialect(parent.getContainingFile()));
        parent.add(node.getPsi());

        return node.getPsi();
    }

    public static PsiElement addExpression(PsiElement parent, String statement)
    {
        ASTNode node = JSChangeUtil.createExpressionFromText(parent.getProject(), statement, JSUtils.getDialect(parent.getContainingFile()));
        parent.add(node.getPsi());

        return node.getPsi();
    }
}
