package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.amd.objectmodel.AMDValidator;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MismatchedImportsDetector
{
    private DojoSettings settingsService;

    public class Mismatch
    {
        private PsiElement define;
        private PsiElement parameter;

        public Mismatch(PsiElement define, PsiElement parameter)
        {
            this.define = define;
            this.parameter = parameter;
        }

        public PsiElement getDefine() {
            return define;
        }

        public PsiElement getParameter() {
            return parameter;
        }
    }

    public List<Mismatch> matchOnList(PsiElement[] defines, PsiElement[] parameters, Map<String, String> exceptions)
    {
        List<Mismatch> results = new ArrayList<Mismatch>();

        if(defines.length < parameters.length)
        {
            // special case where there are missing defines most likely
            for(int i=defines.length;i<parameters.length;i++)
            {
                results.add(new Mismatch(null, parameters[i]));
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

            if(!new AMDValidator().defineMatchesParameter(defines[i].getText(), parameters[i].getText(), exceptions))
            {
                results.add(new Mismatch(defines[i], parameters[i]));
            }
        }

        return results;
    }
}
