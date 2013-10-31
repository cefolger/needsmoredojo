package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.AMDImport;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSParameterList;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;

import java.util.List;

public class SendToEndAction extends SendToAction
{
    @Override
    protected String getName() {
        return "Send AMD Import to End";
    }

    @Override
    protected void moveAction(AnActionEvent e, PsiElement define, PsiElement parameter, List<PsiElement> defines, List<PsiElement> parameters, DefineStatement defineStatement)
    {
        // go to the last literal/parameter pair
        PsiElement lastLiteral = null;
        PsiElement lastParameter = null;

        if(defines.size() > parameters.size())
        {
            lastLiteral = defines.get(parameters.size()-1);
            lastParameter = parameters.get(parameters.size()-1);
        }
        else
        {
            lastLiteral = defines.get(defines.size()-1);
            lastParameter = parameters.get(defines.size()-1);
        }

        final PsiElement finalLastLiteral = lastLiteral;
        final PsiElement finalLastParameter = lastParameter;

        moveElementToEnd(define, parameter, finalLastLiteral, finalLastParameter, defineStatement);
    }

    private PsiElement moveImportToEnd(JSArrayLiteralExpression imports, JSParameterList parameters, String module, String parameter, PsiElement lastDefine, PsiElement lastParameter)
    {
        // TODO move to AMDPsiUtil if we need to reuse this in the future
        PsiElement lastChild = imports.getChildren()[imports.getChildren().length-1];

        if(lastDefine != null)
        {
            lastChild = lastDefine;
        }

        PsiElement element = imports.addAfter(JSChangeUtil.createExpressionFromText(imports.getProject(), String.format("%s", module)).getPsi(), lastChild);
        imports.getNode().addLeaf(JSTokenTypes.COMMA, ",", element.getNode());
        imports.getNode().addLeaf(JSTokenTypes.WHITE_SPACE, "\n", element.getNode());

        PsiElement lastParameterChild = parameters.getChildren()[parameters.getChildren().length-1];

        if(lastParameter != null)
        {
            lastParameterChild = lastParameter;
        }

        PsiElement parameterElement = parameters.addAfter(JSChangeUtil.createExpressionFromText(imports.getProject(), String.format("%s", parameter)).getPsi(), lastParameterChild);
        parameters.getNode().addLeaf(JSTokenTypes.COMMA, ",", parameterElement.getNode());

        return element;
    }

    private void moveElementToEnd(PsiElement define, PsiElement parameter, PsiElement lastDefine, PsiElement lastParameter, DefineStatement defineStatement)
    {
        if(lastDefine.equals(define))
        {
            return;
        }

        PsiElement ignoreComment = AMDPsiUtil.getIgnoreCommentAfterLiteral(define);
        PsiElement newElement = moveImportToEnd(defineStatement.getArguments(), defineStatement.getFunction().getParameterList(), define.getText(), parameter.getText(), lastDefine, lastParameter);

        if(ignoreComment != null)
        {
            defineStatement.getArguments().addAfter(ignoreComment, newElement);
        }

        AMDPsiUtil.removeSingleImport(new AMDImport((JSElement) define, (JSElement)parameter));
    }
}
