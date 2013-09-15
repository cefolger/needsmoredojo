package com.chrisfolger.needsmoredojo.core.amd.define.organizer;

import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class is responsible for sorting AMD imports. This requires the following steps
 *  - gather all imports in a define statemnet (easy)
 *  - sort them (easy)
 *  - reorder the PsiElements (difficult)
 */
public class AMDImportOrganizer
{
    /**
     * this method replaces an unsorted array of PsiElements with a sorted version. This is meant to sort
     * AMD imports in a define(...) call
     *
     * When replacing, it also normalizes quotes
     *
     * @param unsorted the array of unsorted elements to sort
     * @param sorted the sorted array of elements
     * @param deleteTrailingComma if true, trailing commas after all reorganizing is done will be deleted
     * @param result this is used to determine which quote (' or "") to use for normalizing
     */
    public void reorder(PsiElement[] unsorted, SortedPsiElementAdapter[] sorted, boolean deleteTrailingComma, SortingResult result)
    {
        boolean enableLineCommentPreservation = false;
        String parentString = "";
        if(unsorted.length > 0)
        {
            parentString = unsorted[0].getParent().getText();
            int count = StringUtils.countMatches(parentString, "\n");
            // in this special case, where each define literal is on its own line, it's safe to
            // try and preserve line comment order.
            // else, assume we can't be smart enough to try that.
            if(count >= unsorted.length)
            {
                enableLineCommentPreservation = true;
            }
        }

        PsiElement parent = unsorted[0].getParent();

        char quote = '\'';
        if(result.getDoubleQuotes() > result.getSingleQuotes())
        {
            quote = '\"';
        }

        List<PsiElement> deleteList = new ArrayList<PsiElement>();

        // make the sorted ones copies of the original
        // replace the unsorted ones
        for(int i=0;i<unsorted.length;i++)
        {
            if(!sorted[i].isInactive())
            {
                PsiElement newElement = null;
                newElement = sorted[i].getElement().copy();
                Project project = sorted[i].getElement().getProject();
                String text = newElement.getText();
                char mark = text.charAt(0);

                if(mark == '\'' || mark == '\"')
                {
                    String newElementText = text.replace(text.charAt(0), quote);
                    newElement = JSChangeUtil.createExpressionFromText(project, newElementText).getPsi();
                }


                // check to see if the element has a NMD:Ignore comment. I know this adds coupling but
                // in the future maybe it can be made more generic.
                // we have to delete it because this might be from a different import
                PsiElement ignoreComment = AMDPsiUtil.getIgnoreCommentAfterLiteral(unsorted[i]);
                PsiElement regularComment = AMDPsiUtil.getNonIgnoreCommentAfterLiteral(unsorted[i]);

                if(ignoreComment != null)
                {
                    deleteList.add(ignoreComment);
                }

                if(sorted[i].getIgnoreComment() != null)
                {
                    unsorted[i].getParent().addAfter(sorted[i].getIgnoreComment(), unsorted[i]);
                }

                // also check if there is a regular comment and move it.
                // this is only for the case where the define literals are separated by lines and there is
                // a comment at the end of the line (see dijit/layout/ContentPane for an example)
                if(enableLineCommentPreservation && regularComment != null)
                {
                    deleteList.add(regularComment);
                }

                if(enableLineCommentPreservation && sorted[i].getRegularComment() != null)
                {
                    PsiElement terminator = AMDPsiUtil.getNextDefineTerminator(unsorted[i]);
                    unsorted[i].getParent().addBefore(sorted[i].getRegularComment(), terminator);
                }

                unsorted[i].replace(newElement);
            }
            else
            {
                deleteList.add(unsorted[i]);
                deleteList.add(unsorted[i]);
                if(unsorted[i].getNextSibling() != null && deleteTrailingComma)
                {
                    deleteList.add(unsorted[i].getNextSibling());
                }
            }
        }

        for(int i=0;i<deleteList.size();i++)
        {
            try
            {
                deleteList.get(i).delete();
            }
            catch(Exception e)
            {
                // sometimes deleting comma's will throw an exception (ignorant as to why)
            }
        }

        /*
         sometimes you get a trailing comma after removing duplicate imports. Instead of trying to track
         the cases where this happens, just check for the presence of a trailing comma and delete if necessary
         */
        if(deleteTrailingComma)
        {
            try
            {
                PsiElement trailingComma = AMDPsiUtil.getNearestComma(parent.getLastChild());
                if(trailingComma != null)
                {
                    trailingComma.delete();
                }
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
    }

    private List<SortItem> getSortItems(List<PsiElement> defines, List<PsiElement> parameters)
    {
        // first create a list of SortItems
        List<SortItem> items = new ArrayList<SortItem>();
        for(int i=0;i<Math.max(defines.size(), parameters.size());i++)
        {
            PsiElement define = null;
            PsiElement parameter = null;

            if(parameters.size() > i)
            {
                parameter = parameters.get(i);
            }

            PsiComment ignoreComment = null;
            PsiComment regularComment = null;

            if(defines.size() > i)
            {
                define = defines.get(i);
                ignoreComment = (PsiComment) AMDPsiUtil.getIgnoreCommentAfterLiteral(define);
                regularComment = (PsiComment) AMDPsiUtil.getNonIgnoreCommentAfterLiteral(define);
            }

            items.add(new SortItem(define, parameter, false, ignoreComment, regularComment));
        }

        return items;
    }

    /**
     * this is the entry point when you want to sort a list of AMD imports alphabetically.
     *
     * @param defines a list of imports coming from the define array literal
     * @param parameters a list of parameters that come from the define function
     * @return the sorting result that describes which quote style to use
     */
    public SortingResult sortDefinesAndParameters(List<PsiElement> defines, List<PsiElement> parameters)
    {
        // first create a list of SortItems
        List<SortItem> items = getSortItems(defines, parameters);

        // sort based on define
        Collections.sort(items, new Comparator<SortItem>() {
            @Override
            public int compare(SortItem o1, SortItem o2)
            {
                // if a parameter is null, it means that it's either a plugin or a module that's loaded but
                // never referenced (ie. chart types, some node list extensions)
                if(o1.getParameter() == null)
                {
                    return 1; // always greater than the next one
                }
                else if (o2.getParameter() == null)
                {
                    return -1; // always less than a plugin
                }

                // if a define is null, this is an extra parameter so it needs to be stuck on the end
                if(o1.getDefine() == null)
                {
                    return 1;
                }
                else if (o2.getDefine() == null)
                {
                    return -1;
                }

                String o1Text = o1.getDefine().getText().replaceAll("'|\"", "");
                String o2Text = o2.getDefine().getText().replaceAll("'|\"", "");

                return o1Text.compareToIgnoreCase(o2Text);
            }
        });

        for(int i=1;i<items.size();i++)
        {
            SortItem item = items.get(i);
            PsiElement firstParameter = item.getParameter();
            PsiElement secondParameter = items.get(i-1).getParameter();

            if(firstParameter != null && secondParameter != null && firstParameter.getText().equals(secondParameter.getText()))
            {
                item.setInactive(true);
            }
        }

        // now generate new lists
        SortedPsiElementAdapter[] sortedDefines = new SortedPsiElementAdapter[defines.size()];
        SortedPsiElementAdapter[] sortedParameters = new SortedPsiElementAdapter[parameters.size()];

        int singleQuotes = 0;
        int doubleQuotes = 0;

        for(int i=0;i<items.size();i++)
        {
            if(sortedDefines.length > i)
            {
                if(items.get(i).getDefine().getText().startsWith("'"))
                {
                    singleQuotes++;
                }
                else
                {
                    doubleQuotes++;
                }

                sortedDefines[i] = SortedPsiElementAdapter.fromDefine(items.get(i));
            }

            if(sortedParameters.length > i)
            {
                sortedParameters[i] = SortedPsiElementAdapter.fromParameter(items.get(i));
            }
        }

        SortingResult result = new SortingResult(sortedDefines, sortedParameters);
        result.setSingleQuotes(singleQuotes);
        result.setDoubleQuotes(doubleQuotes);

        return result;
    }
}
