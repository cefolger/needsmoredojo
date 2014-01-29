package com.chrisfolger.needsmoredojo.core.amd.naming;

import com.chrisfolger.needsmoredojo.core.amd.importing.ImportReorderer;
import com.chrisfolger.needsmoredojo.core.amd.objectmodel.AMDValidator;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MismatchedImportsDetector
{
    public class Mismatch
    {
        private int index;
        private PsiElement define;
        private PsiElement parameter;
        private String absolutePath;

        public Mismatch(PsiElement define, PsiElement parameter, int index, String absolutePath)
        {
            this.absolutePath = absolutePath;
            this.define = define;
            this.parameter = parameter;
            this.index = index;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }

        public int getIndex() {
            return index;
        }

        public PsiElement getDefine() {
            return define;
        }

        public PsiElement getParameter() {
            return parameter;
        }
    }

    public List<Mismatch> matchOnList(PsiElement[] defines, PsiElement[] parameters, Map<String, String> exceptions, DojoSettings dojoSettings)
    {
        List<Mismatch> results = new ArrayList<Mismatch>();

        if(defines.length < parameters.length)
        {
            // special case where there are missing defines most likely
            for(int i=defines.length;i<parameters.length;i++)
            {
                results.add(new Mismatch(null, parameters[i], i, null));
            }
        }

        // we always go through parameters instead of defines, because parameter list will not include plugins
        // and other stuff
        for(int i=0;i<parameters.length;i++)
        {
            if(i >= defines.length)
            {
                continue; // we've already accounted for the mismatch here
            }

            String defineTest = defines[i].getText();
            String parameterTest = parameters[i].getText();
            String absolutePath = null;
            // TODO option to enable resolution based inspection
            // TODO fix add new import
            boolean enableResolutionBasedInspection = true;
            if(enableResolutionBasedInspection)
            {
                ImportReorderer reorderer = new ImportReorderer();
                String absoluteModulePath = reorderer.getAbsoluteSyntax(defines[i], defines[i].getContainingFile());

                if(absoluteModulePath != null)
                {
                    defineTest = absoluteModulePath;
                    absolutePath = absoluteModulePath;
                }
            }

            if(!new AMDValidator().defineMatchesParameter(defineTest, parameterTest, exceptions))
            {
                results.add(new Mismatch(defines[i], parameters[i], i, absolutePath));
            }
        }

        return results;
    }
}
