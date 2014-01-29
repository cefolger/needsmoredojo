package com.chrisfolger.needsmoredojo.core.amd.importing;

import com.chrisfolger.needsmoredojo.core.amd.define.DefineResolver;
import com.chrisfolger.needsmoredojo.core.amd.define.DefineStatement;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.DojoModuleFileResolver;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourceLibrary;
import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourcesLocator;
import com.chrisfolger.needsmoredojo.core.amd.naming.NameResolver;
import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.chrisfolger.needsmoredojo.core.util.PsiUtil;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        DefineStatement items = new DefineResolver().getDefineStatementItemsFromArguments(list.getArguments(), null);

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

    /**
     * Given a module import, converts it to the absolute path reference. If it's already an absolute path,
     * returns it instead of trying to resolve it
     *
     * @param define
     * @param file
     * @return
     */
    public String getAbsoluteSyntax(PsiElement define, PsiFile file)
    {
        if(define == null)
        {
            return null;
        }

        boolean relative = define.getText().charAt(1) == '.';
        String moduleText = define.getText().replaceAll("'", "").replaceAll("\"", "");

        if(!relative)
        {
            return moduleText;
        }

        String moduleName = NameResolver.getModuleName(moduleText);
        String resourceId = NameResolver.getAMDPluginResourceIfPossible(moduleText, true);

        // get the list of possible strings/PsiFiles that would match it
        PsiFile importedFile = new DojoModuleFileResolver().resolveReferencedFile(define.getProject(), define);

        // get the files that are being imported
        PsiFile[] files = new ImportResolver().getPossibleDojoImportFiles(file.getProject(), moduleName, true, false);
        LinkedHashMap<String, PsiFile> results = new ImportResolver().getChoicesFromFiles(files,
                new SourcesLocator().getSourceLibraries(file.getProject()).toArray(new SourceLibrary[0]),
                moduleName,
                define.getContainingFile(),
                false,
                true);

        // filter results based the file
        List<String> choices = new ArrayList<String>();
        for(Map.Entry<String, PsiFile> entry : results.entrySet())
        {
            if(entry.getValue().equals(importedFile))
            {
                choices.add(entry.getKey());
            }
        }


        String choice = choices.get(0) + resourceId;

        if(choices.size() > 1)
        {
            if (!choices.get(1).startsWith("."))
            {
                return choices.get(1) + resourceId;
            }
        }

        return choice;
    }

    /**
     * Given a module that is being imported using absolute or relative path syntax, return the module
     * import with the other syntax, if possible.
     *
     * @param define
     * @param file
     * @return
     */
    public @Nullable PsiElement getOppositePathSyntaxFromImport(PsiElement define, PsiFile file)
    {
        if(define == null)
        {
            return null;
        }

        boolean relative = define.getText().charAt(1) == '.';
        char quote = define.getText().charAt(0);
        String moduleText = define.getText().replaceAll("'", "").replaceAll("\"", "");
        String moduleName = NameResolver.getModuleName(moduleText);
        String resourceId = NameResolver.getAMDPluginResourceIfPossible(moduleText, true);

        // get the list of possible strings/PsiFiles that would match it
        PsiFile[] files = new ImportResolver().getPossibleDojoImportFiles(file.getProject(), moduleName, true, false);

        // get the files that are being imported
        String[] results = new ImportResolver().getChoicesFromFiles(files, new SourcesLocator().getSourceLibraries(file.getProject()).toArray(new SourceLibrary[0]), moduleName, define.getContainingFile(), false);
        String choice = results[0] + resourceId;

        if(results.length > 1)
        {
            if(results[1].startsWith(".") && !relative)
            {
                choice = results[1] + resourceId;
            }
            else if (!results[1].startsWith(".") && relative)
            {
                choice = results[1] + resourceId;
            }
        }

        if(choice.equals(moduleText))
        {
            return null; // no point in replacing with the same thing
        }

        PsiElement replacement = JSChangeUtil.createExpressionFromText(define.getProject(), quote + choice + quote).getPsi();

        return replacement;
    }
}
