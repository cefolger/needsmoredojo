package com.chrisfolger.needsmoredojo.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestAMDUtil
{
    @Test
    public void dojoModuleWithHyphenConvertedCorrectly()
    {
        assertEquals("domConstruct", AMDUtil.defineToParameter("dojo/dom-construct"));
    }

    @Test
    public void testModuleWithUnderscore()
    {
        assertEquals("WidgetBase", AMDUtil.defineToParameter("dijit/_WidgetBase"));
    }

    @Test
    public void testBasicModule()
    {
        assertEquals("BorderContainer", AMDUtil.defineToParameter("dijit/layout/BorderContainer"));
    }

    @Test
    public void testBaseFx()
    {
        assertEquals("baseFx", AMDUtil.defineToParameter("dojo/_base/fx"));
    }

    @Test
    public void testTextPlugin()
    {
        assertEquals("amoduleTemplate", AMDUtil.defineToParameter("dojo/text!./AModule.html"));
    }

    @Test
    public void testI18nPlugin()
    {
        assertEquals("resources", AMDUtil.defineToParameter("dojo/i18n!nls/resources"));
    }
}
