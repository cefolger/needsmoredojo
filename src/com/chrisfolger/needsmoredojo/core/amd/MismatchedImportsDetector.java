package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.chrisfolger.needsmoredojo.core.util.AMDUtil;
import com.intellij.psi.PsiElement;

import javax.jnlp.ServiceManager;
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

            if(!defineMatchesParameter(defines[i].getText(), parameters[i].getText(), exceptions))
            {
                results.add(new Mismatch(defines[i], parameters[i]));
            }
        }

        return results;
    }

    public boolean defineMatchesParameter(String define, String parameter, Map<String, String> exceptions)
    {
        // simple case can be taken care of by just matching the stuff after / with the parameter
        // also case insensitive because the programmer can use any casing for the parameter
        String defineComparison = define.toLowerCase().replaceAll("'|\"", "").replace("\"", "");
        String parameterComparison = parameter.toLowerCase();

        if(exceptions.containsKey(defineComparison))
        {
            return parameterComparison.equals(exceptions.get(defineComparison));
        }

        if(defineComparison.contains("/_base/fx"))
        {
            return parameterComparison.equals("basefx") || parameterComparison.equals("fx");
        }

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
        if(defineComparison.startsWith(AMDUtil.TEXTPLUGIN) || defineComparison.startsWith(AMDUtil.I18NPLUGIN))
        {
            // grab everything after the !
            String fileName = defineComparison.substring(defineComparison.lastIndexOf('!') + 1);

            if(fileName.indexOf('/') != -1)
            {
                fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            }

            if(fileName.indexOf('.') != -1)
            {
                fileName = fileName.substring(0, fileName.indexOf('.'));
            }

            boolean isText = defineComparison.startsWith(AMDUtil.TEXTPLUGIN);
            boolean isI18n = defineComparison.startsWith(AMDUtil.I18NPLUGIN);

            if(isText && parameterComparison.contains(fileName + "template"))
            {
                return true;
            }
            else if(isText && parameterComparison.equals("template"))
            {
                return true;
            }
            // can't really enforce a stricter convention in this case
            else if(isI18n && parameterComparison.startsWith("i18n"))
            {
                return true;
            }
            else if (isI18n && parameterComparison.startsWith("resources"))
            {
                return true;
            }
            else if(isI18n && parameterComparison.startsWith("nls"))
            {
                return true;
            }
            else if(isI18n && parameterComparison.startsWith("_nls"))
            {
                return true;
            }

            return fileName.contains(parameterComparison);
        }

        return false;
    }
}
