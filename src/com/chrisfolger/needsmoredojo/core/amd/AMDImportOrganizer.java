package com.chrisfolger.needsmoredojo.core.amd;

import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

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
    public class SortingResult
    {
        private SortedPsiElementAdapter[] defines;
        private SortedPsiElementAdapter[] parameters;
        private int singleQuotes;
        private int doubleQuotes;

        public SortingResult(SortedPsiElementAdapter[] defines, SortedPsiElementAdapter[] parameters)
        {
            this.defines = defines;
            this.parameters = parameters;
        }

        public SortedPsiElementAdapter[] getParameters() {
            return parameters;
        }

        public void setParameters(SortedPsiElementAdapter[] parameters) {
            this.parameters = parameters;
        }

        public SortedPsiElementAdapter[] getDefines() {
            return defines;
        }

        public void setDefines(SortedPsiElementAdapter[] defines) {
            this.defines = defines;
        }

        public int getSingleQuotes() {
            return singleQuotes;
        }

        public void setSingleQuotes(int singleQuotes) {
            this.singleQuotes = singleQuotes;
        }

        public int getDoubleQuotes() {
            return doubleQuotes;
        }

        public void setDoubleQuotes(int doubleQuotes) {
            this.doubleQuotes = doubleQuotes;
        }
    }

    public class SortedPsiElementAdapter
    {
        private PsiElement element;
        private boolean inactive;

        public SortedPsiElementAdapter(PsiElement element, boolean inactive) {
            this.element = element;
            this.inactive = inactive;
        }

        public PsiElement getElement() {
            return element;
        }

        public void setElement(PsiElement element) {
            this.element = element;
        }

        public boolean isInactive() {
            return inactive;
        }

        public void setInactive(boolean inactive) {
            this.inactive = inactive;
        }
    }

    private class SortItem
    {
        private PsiElement define;
        private PsiElement parameter;
        private boolean inactive;

        public SortItem(PsiElement define, PsiElement parameter, boolean inactive)
        {
            this.define = define;
            this.parameter = parameter;
        }

        public PsiElement getDefine() {
            return define;
        }

        public void setDefine(PsiElement define) {
            this.define = define;
        }

        public PsiElement getParameter() {
            return parameter;
        }

        public void setParameter(PsiElement parameter) {
            this.parameter = parameter;
        }

        public boolean isInactive() {
            return inactive;
        }

        public void setInactive(boolean inactive) {
            this.inactive = inactive;
        }
    }

    public void reorder(PsiElement[] unsorted, SortedPsiElementAdapter[] sorted, boolean deleteTrailingComma, SortingResult result)
    {
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

                if(mark != quote && (mark == '\'' || mark == '\"'))
                {
                    newElement = JSChangeUtil.createExpressionFromText(project,text.replace(text.charAt(0), quote)).getPsi();
                }

                unsorted[i].replace(newElement);
            }
            else
            {
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
                deleteList.get(i).delete();;
            }
            catch(Exception e)
            {
                // sometimes deleting comma's will throw an exception (ignorant as to why)
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

            if(defines.size() > i)
            {
                define = defines.get(i);
            }

            items.add(new SortItem(define, parameter, false));
        }

        return items;
    }

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

                sortedDefines[i] = new SortedPsiElementAdapter(items.get(i).getDefine(), items.get(i).isInactive());
            }

            if(sortedParameters.length > i)
            {
                sortedParameters[i] = new SortedPsiElementAdapter(items.get(i).getParameter(), items.get(i).isInactive());
            }
        }

        SortingResult result = new SortingResult(sortedDefines, sortedParameters);
        result.setSingleQuotes(singleQuotes);
        result.setDoubleQuotes(doubleQuotes);

        return result;
    }
}
