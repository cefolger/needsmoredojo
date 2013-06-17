package com.chrisfolger.needsmoredojo.conventions;

import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

public class MismatchedImportsDetector
{
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

    public List<Mismatch> matchOnList(PsiElement[] defines, PsiElement[] parameters)
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

            if(!defineMatchesParameter(defines[i].getText(), parameters[i].getText()))
            {
                results.add(new Mismatch(defines[i], parameters[i]));
            }
        }

        return results;
    }

    public static String defineToParameter(String define)
    {
        if(define.startsWith("dojo/text") || define.startsWith("dojo/i18n"))
        {
            return define.substring(define.indexOf('!') + 1);
        }

        String result = define.substring(define.lastIndexOf("/") + 1);
        if(result.contains("-"))
        {
            int index = result.indexOf('-');
            result = result.replace("-", "");
            result = result.substring(0,index)+ ("" +result.charAt(index)).toUpperCase() +result.substring(index+1);
        }

        result = result.replaceAll("_", "");

        return result;
    }

    public boolean defineMatchesParameter(String define, String parameter)
    {
        // simple case can be taken care of by just matching the stuff after / with the parameter
        // also case insensitive because the programmer can use any casing for the parameter
        String defineComparison = define.toLowerCase().replaceAll("'|\"", "").replace("\"", "");
        String parameterComparison = parameter.toLowerCase();

        if(defineComparison.indexOf('/') != -1)
        {
            String defineName = defineComparison.substring(defineComparison.lastIndexOf('/') + 1);

            if(defineName.equals(parameterComparison))
            {
                return true;
            }

            // stuff like dom-construct is often referenced as domConstruct and should be considered valid
            if(defineName.contains("-") && defineName.replace("-", "").equals(parameterComparison))
            {
                return true;
            }

            // other stuff such as _WidgetBase needs to be accounted for
            if(defineName.startsWith("_") && defineName.substring(1).equals(parameterComparison))
            {
                return true;
            }
        }
        else
        {
            if(defineComparison.equals(parameterComparison))
            {
                return true;
            }
        }

        // there is a hard-coded comparison against dojo/text since it is used to define templates
        if(defineComparison.startsWith("dojo/text"))
        {
            // grab everything after the !
            String fileName = defineComparison.substring(defineComparison.lastIndexOf('!') + 1);
            return fileName.contains(parameterComparison);
        }

        return false;
    }
}
