package com.chrisfolger.needsmoredojo.core.amd.naming;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class NameResolver
{
    public static final String I18NPLUGIN = "dojo/i18n!";
    public static final String TEXTPLUGIN = "dojo/text!";

    public static String defineToParameter(String define, Map<String, String> exceptions)
    {
        define = define.replaceAll("\"|'", "");

        // since there are two fx modules we have this exception
        if(define.contains("/_base/fx"))
        {
            return "baseFx";
        }

        // check all exceptions
        if(exceptions.containsKey(define))
        {
            return exceptions.get(define);
        }

        if(define.startsWith(TEXTPLUGIN) || define.startsWith(I18NPLUGIN))
        {
            String postPlugin = define.substring(define.indexOf('!') + 1);

            if(postPlugin.indexOf('/') != -1)
            {
                postPlugin = postPlugin.substring(postPlugin.lastIndexOf('/') + 1);
            }

            if(postPlugin.indexOf('.') != -1)
            {
                postPlugin = postPlugin.substring(0, postPlugin.indexOf('.'));
            }

            if(!define.startsWith(I18NPLUGIN))
            {
                postPlugin = postPlugin.toLowerCase() + "Template";
            }

            return postPlugin;
        }

        String result = define.substring(define.lastIndexOf("/") + 1);
        if(result.contains("-"))
        {
            int index = result.indexOf('-');
            result = result.replace("-", "");
            result = result.substring(0,index)+ ("" +result.charAt(index)).toUpperCase() +result.substring(index+1);
        }

        result = result.replaceAll("_", "");

        if(result.contains("!"))
        {
            result = result.substring(0, result.indexOf('!'));
        }

        return result;
    }

    /**
     * if a module uses the plugin syntax, returns only the actual plugin name
     *
     * @param module the possible plugin module
     * @return the module name
     */
    public static String getAMDPluginNameIfPossible(String module)
    {
        if(module.indexOf('!') > 0 && module.indexOf('/') != 0)
        {
            return module.substring(module.lastIndexOf('/') + 1, module.lastIndexOf('!'));
        }
        else if(module.indexOf('!') > 0)
        {
            return module.substring(0, module.lastIndexOf('!'));
        }
        else
        {
            return module;
        }
    }

    /**
     * given a module, returns the resource id if it's an AMD plugin
     *
     * @param module
     * @param includeExclamationPoint
     * @return
     */
    public static String getAMDPluginResourceIfPossible(String module, boolean includeExclamationPoint)
    {
        if(module.indexOf('!') > 0)
        {
            if(includeExclamationPoint)
            {
                return module.substring(module.indexOf('!'));
            }
            else
            {
                return module.substring(module.indexOf('!') + 1);
            }
        }
        else
        {
            return "";
        }
    }

    public static String getModuleName(String modulePath)
    {
        modulePath = modulePath.replaceAll("'|\"", "");

        if(modulePath.contains("!"))
        {
            String moduleWithoutPlugin = modulePath.substring(0, modulePath.indexOf('!'));
            return moduleWithoutPlugin.substring(moduleWithoutPlugin.lastIndexOf('/') + 1);
        }
        else
        {
            return modulePath.substring(modulePath.lastIndexOf('/') + 1);
        }
    }

    /**
     * converts a module to its correct hyphenated form if possible.
     *
     * For example, domClass becomes dom-class, domAttr becomes dom-attr, etc.
     *
     * @param module
     * @return the hyphenated module, if possible. OR null if it wasn't converted
     */
    public static @Nullable String getPossibleHyphenatedModule(@NotNull String module)
    {
        try
        {
            // the obvious ones we're going for
            if(module.startsWith("dom"))
            {
                return "dom-" + module.substring(3).toLowerCase();
            }

            // otherwise convert it if you use a camel-case convention
            // http://stackoverflow.com/a/7599674/324992
            String[] terms = module.split("(?<=[a-z])(?=[A-Z])");
            StringBuilder result = new StringBuilder(terms[0]);
            result.append("-");

            for(int i=1;i<terms.length;i++)
            {
                result.append(terms[i].toLowerCase());
            }

            return result.toString();
        }
        catch(Exception e)
        {
            // Yes, this is a catch all, but for a reason. If anything bad happens in here,
            // we just want to return null. We don't want to have to worry about this method
            // failing where this is being consumed.
            return null;
        }
    }

    /**
     * Dojo relative paths use "./" for same level and "../" for additional levels.
     * This will ensure that a relative path passed to the method returns a dojo-friendly version
     *
     * @param relativePath
     * @return
     */
    public static String convertRelativePathToDojoPath(String relativePath)
    {
        if(relativePath != null)
        {
            // need to use dojo syntax when two files are in the same directory
            if(relativePath.equals("."))
            {
                relativePath = "./";
            }
            else if (relativePath.charAt(0) != '.' && relativePath.charAt(0) != '/')
            {
                // top level module
                relativePath = "./" + relativePath;
            }

            return relativePath;
        }

        return null;
    }

    public static String getModulePath(String fullModulePath)
    {
        String modulePath = fullModulePath;
        if(modulePath.contains("!"))
        {
            modulePath = modulePath.substring(0, modulePath.lastIndexOf('!'));
        }
        modulePath = modulePath.substring(0, modulePath.lastIndexOf('/') + 1);

        return modulePath;
    }
}
