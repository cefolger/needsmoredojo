package com.chrisfolger.needsmoredojo.core.amd.importing;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.chrisfolger.needsmoredojo.core.util.PsiUtil;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.psi.PsiElement;

public class ImportReorderer
{

    /**
     * given an AMD literal that the cursor is over and a direction, finds the literal to swap it with
     *
     * @param element the source element
     * @param direction whether to swap with the element below the source or above it
     * @return an array containing two elements ... the source literal and the destination.
     *  OR an array of size 0 if none were found or were invalid.
     */
    public PsiElement[] getSourceAndDestination(PsiElement element, AMDPsiUtil.Direction direction)
    {
        JSLiteralExpression source = null;

        if(element == null)
        {
            return new PsiElement[0];
        }

        if(element instanceof JSLiteralExpression)
        {
            source = (JSLiteralExpression) element;
        }
        else if (element.getParent() instanceof JSLiteralExpression)
        {
            source = (JSLiteralExpression) element.getParent();
        }
        else
        {
            source = AMDPsiUtil.getNearestLiteralExpression(element, AMDPsiUtil.Direction.UP);
        }

        if(source == null)
        {
            // cursor wasn't in the right spot
            return new PsiElement[0];
        }
        else if (direction == AMDPsiUtil.Direction.NONE)
        {
            return new PsiElement[] { source };
        }

        // find destination
        JSLiteralExpression destination = null;
        if(direction == AMDPsiUtil.Direction.UP)
        {
            destination = AMDPsiUtil.getNearestLiteralExpression(source.getPrevSibling(), direction);
        }
        else
        {
            destination = AMDPsiUtil.getNearestLiteralExpression(source.getNextSibling(), direction);
        }

        if(destination == null || source == null)
        {
            return new PsiElement[0];
        }

        return new PsiElement[] { source, destination };
    }

    public PsiElement[] reorder(PsiElement source, PsiElement destination)
    {
        PsiElement[] results = new PsiElement[2];

        results[0] = destination.replace(source);
        results[1] = source.replace(destination);

        return results;
    }

    public void doSwap(PsiElement source, Editor editor, AMDPsiUtil.Direction direction)
    {
        PsiElement[] defines = getSourceAndDestination(source, direction);

        if(defines == null || defines.length == 0)
        {
            return;
        }

        // get the parameter element
        JSArgumentList list = (JSArgumentList) defines[0].getParent().getParent();
        DefineStatement items = new DefineResolver().getDefineStatementItemsFromArguments(list.getArguments());

        int sourceIndex = PsiUtil.getIndexInParent(defines[0]);
        int destinationIndex = PsiUtil.getIndexInParent(defines[1]);
        JSParameter[] parameterList = items.getFunction().getParameters();

        if(sourceIndex >= parameterList.length || destinationIndex >= parameterList.length)
        {
            // we're moving into a plugin's position
            return;
        }

        PsiElement[] parameters = new PsiElement[] { parameterList[sourceIndex], parameterList[destinationIndex] };

        PsiElement[] elementsWithPositions = reorder(defines[0], defines[1]);
        reorder(parameters[0], parameters[1]);

        editor.getCaretModel().moveToOffset(elementsWithPositions[0].getTextOffset());
        editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
    }
}
