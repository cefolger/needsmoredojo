package com.chrisfolger.needsmoredojo.core.util;

public class AMDUtil
{
    public static String defineToParameter(String define)
    {
        // since there are two fx modules we have this exception
        if(define.contains("/_base/fx"))
        {
            return "baseFx";
        }

        if(define.startsWith("dojo/text") || define.startsWith("dojo/i18n"))
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

            if(!define.startsWith("dojo/i18n"))
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

        return result;
    }
}
